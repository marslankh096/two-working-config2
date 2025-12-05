
package com.ahmadullahpk.alldocumentreader.xs.system;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.ahmadullahpk.alldocumentreader.widgets.mricheditor.FontStyle;
import com.ahmadullahpk.alldocumentreader.xs.common.ICustomDialog;
import com.ahmadullahpk.alldocumentreader.xs.common.ISlideShow;
import com.ahmadullahpk.alldocumentreader.xs.common.picture.PictureKit;
import com.ahmadullahpk.alldocumentreader.xs.constant.MainConstant;
import com.ahmadullahpk.alldocumentreader.xs.fc.doc.TXTKit;
import com.ahmadullahpk.alldocumentreader.xs.pg.control.PGControl;
import com.ahmadullahpk.alldocumentreader.xs.pg.control.Presentation;
import com.ahmadullahpk.alldocumentreader.xs.pg.model.PGModel;
import com.ahmadullahpk.alldocumentreader.xs.simpletext.model.IDocument;
import com.ahmadullahpk.alldocumentreader.xs.ss.control.SSControl;
import com.ahmadullahpk.alldocumentreader.xs.ss.model.baseModel.Workbook;
import com.ahmadullahpk.alldocumentreader.xs.wp.control.WPControl;
import com.ahmadullahpk.alldocumentreader.xs.constant.EventConstant;
import com.ahmadullahpk.alldocumentreader.xs.wp.control.Word;

public class MainControl extends AbstractControl {
    private IControl appControl;
    private ICustomDialog customDialog;
    private String filePath;
    private IMainFrame frame;
    private Handler handler;
    private boolean isAutoTest;
    public boolean isCancel;
    private boolean isDispose;
    //    private IOfficeToPicture officeToPicture;
    private DialogInterface.OnKeyListener onKeyListener;
    //    private ProgressDialog progressDialog;
    private IReader reader;
    private ISlideShow slideShow;
    private Toast toast;
    private AUncaughtExceptionHandler uncaught;
    private byte applicationType = -1;
    public SysKit sysKit;

    @Override
    public Dialog getDialog(Activity activity, int id) {
        return null;
    }

    @Override
    public void layoutView(int x, int y, int w, int h) {
    }

    public MainControl(IMainFrame frame) {
        this.frame = frame;
        AUncaughtExceptionHandler aUncaughtExceptionHandler = new AUncaughtExceptionHandler(this);
        this.uncaught = aUncaughtExceptionHandler;
        Thread.setDefaultUncaughtExceptionHandler(aUncaughtExceptionHandler);
        sysKit = new SysKit(this);
        init();
    }

    private void init() {
        initListener();
        boolean z = false;
        this.toast = Toast.makeText(getActivity().getApplicationContext(), "", Toast.LENGTH_SHORT);
        String stringExtra = getActivity().getIntent().getStringExtra("autoTest");
        if (stringExtra != null && stringExtra.equals("true")) {
            z = true;
        }
        this.isAutoTest = z;
    }

    @SuppressLint("HandlerLeak")
    private void initListener() {
        this.onKeyListener = (dialog, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK) { // original value 4
                dialog.dismiss();
                MainControl.this.isCancel = true;
                if (MainControl.this.reader != null) {
                    MainControl.this.reader.abortReader();
                    MainControl.this.reader.dispose();
                }
                MainControl.this.getActivity().onBackPressed();
                return true;
            } else {
                return false;
            }
        };
        this.handler = new Handler() {
            @Override
            public void handleMessage(final Message msg) {
                if (!MainControl.this.isCancel) {
                    int i = msg.what;
                    if (i == MainConstant.HANDLER_MESSAGE_SUCCESS) { //0
                        post(() -> {
                            try {
                                if (MainControl.this.getMainFrame().isShowProgressBar()) {

                                    if (frame != null)
                                        frame.dismissLoader();

                                    MainControl.this.dismissProgressDialog();
                                } else if (MainControl.this.customDialog != null) {
                                    MainControl.this.customDialog.dismissDialog((byte) 2);
                                }
                                MainControl.this.createApplication(msg.obj);
                            } catch (Exception exception) {
                                MainControl.this.sysKit.getErrorKit().writerLog(exception, true);
                            }
                        });
                    } else if (i == MainConstant.HANDLER_MESSAGE_ERROR) { //1
                        post(() -> {
                            if (frame != null)
                                frame.dismissLoader();
                            MainControl.this.dismissProgressDialog();
                            if (msg.obj instanceof Throwable) {
                                MainControl.this.sysKit.getErrorKit().writerLog((Throwable) msg.obj, true);
                            }
                        });
                    } else if (i == MainConstant.HANDLER_MESSAGE_SHOW_PROGRESS) { //2
                        if (MainControl.this.getMainFrame().isShowProgressBar()) {
                            post(() -> {
                                if (frame != null)
                                    frame.showLoader();
//                                try{
//                                    MainControl.this.progressDialog = ProgressDialog.show(MainControl.this.getActivity(), MainControl.this.frame.getAppName(), MainControl.this.frame.getLocalString("DIALOG_LOADING"), false, false, null);
//                                    MainControl.this.progressDialog.setOnKeyListener(MainControl.this.onKeyListener);
//                                }catch (Exception a){}
                            });
                        } else if (MainControl.this.customDialog != null) {
//                            try{
//                                MainControl.this.customDialog.showDialog((byte) 2);
//                            }catch (Exception a ){}
                        }
                    } else if (i == MainConstant.HANDLER_MESSAGE_DISMISS_PROGRESS) { // 3
                        if (frame != null)
                            frame.dismissLoader();

                        try {
                            post(MainControl.this::dismissProgressDialog);
                        } catch (Exception e) {
                        }
                    } else if (i == MainConstant.HANDLER_MESSAGE_SEND_READER_INSTANCE) { // 4
                        MainControl.this.reader = (IReader) msg.obj;
                    } else {
                        return;
                    }
                    ///////////////////////////////////////////
//                     if (i != 2) {
//                        if (i == 3) {
//                            post(MainControl.this::dismissProgressDialog);
//                        } else if (i == 4) {
//                            MainControl.this.reader = (IReader) msg.obj;
//                        }
//                    } else if (MainControl.this.getMainFrame().isShowProgressBar()) {
//                        post(() -> {
//                            MainControl.this.progressDialog = ProgressDialog.show(MainControl.this.getActivity(), MainControl.this.frame.getAppName(), MainControl.this.frame.getLocalString("DIALOG_LOADING"), false, false, null);
//                            MainControl.this.progressDialog.setOnKeyListener(MainControl.this.onKeyListener);
//                        });
//                    } else if (MainControl.this.customDialog != null) {
//                        MainControl.this.customDialog.showDialog((byte) 2);
//                    }
                } else {
                    return;
                }
            }
        };
    }

    public void dismissProgressDialog() {
       /* ProgressDialog progressDialog = this.progressDialog;
        if (progressDialog != null) {
            progressDialog.dismiss();
            this.progressDialog = null;
        }
        Handler handler = this.handler;
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }*/
    }

    public void createApplication(Object obj) throws Exception {
        Object viewBackground;
        if (obj != null) {
            byte b = this.applicationType;
            switch (b) {
                case MainConstant.APPLICATION_TYPE_WP:
                    this.appControl = new WPControl(this, (IDocument) obj, this.filePath);
                    break;
                case MainConstant.APPLICATION_TYPE_SS:
                    this.appControl = new SSControl(this, (Workbook) obj, this.filePath);
                    break;
                case MainConstant.APPLICATION_TYPE_PPT:
                    this.appControl = new PGControl(this, (PGModel) obj, this.filePath);
                    break;
            }

            View view = this.appControl.getView();
            if (!(view == null || (viewBackground = this.frame.getViewBackground()) == null)) {
                if (viewBackground instanceof Integer) {
                    view.setBackgroundColor((Integer) viewBackground);
                } else if (viewBackground instanceof Drawable) {
                    view.setBackgroundDrawable((Drawable) viewBackground);
                }
            }

            this.frame.openFileFinish();

            PictureKit.instance().setDrawPictrue(true);
            this.handler.post(() -> {
                if (Build.VERSION.SDK_INT >= 11) {
                    try {
                        View view2 = MainControl.this.getView();
                        Object invoke = view2.getClass().getMethod("isHardwareAccelerated", null).invoke(view2, (Object) null);
                        if (invoke != null && (invoke instanceof Boolean) && (Boolean) invoke) {
                            view2.getClass().getMethod("setLayerType", Integer.TYPE, Paint.class).invoke(view2, view2.getClass().getField("LAYER_TYPE_SOFTWARE").getInt(null), null);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                MainControl.this.actionEvent(EventConstant.SYS_SET_PROGRESS_BAR_ID, false); // 26
                MainControl.this.actionEvent(EventConstant.SYS_INIT_ID, null);//19
                MainControl.this.frame.updateToolsbarStatus();
                MainControl.this.getView().postInvalidate();
            });
            return;
        }
        throw new Exception("Document with password");
    }

    @Override
    public boolean openFile(String filePath) {
        this.filePath = filePath;
        String lowerCase = filePath.toLowerCase();
        if (lowerCase.endsWith(MainConstant.FILE_TYPE_DOC) || lowerCase.endsWith(MainConstant.FILE_TYPE_DOCX) || lowerCase.endsWith(MainConstant.FILE_TYPE_TXT) || lowerCase.endsWith(MainConstant.FILE_TYPE_DOT) || lowerCase.endsWith(MainConstant.FILE_TYPE_DOTX) || lowerCase.endsWith(MainConstant.FILE_TYPE_DOTM)) {
            this.applicationType = (byte) 0;//MainConstant.APPLICATION_TYPE_WP
        } else if (lowerCase.endsWith(MainConstant.FILE_TYPE_XLS) || lowerCase.endsWith(MainConstant.FILE_TYPE_XLSX) || lowerCase.endsWith(MainConstant.FILE_TYPE_XLT) || lowerCase.endsWith(MainConstant.FILE_TYPE_XLTX) || lowerCase.endsWith(MainConstant.FILE_TYPE_XLTM) || lowerCase.endsWith(MainConstant.FILE_TYPE_XLSM)) {
            this.applicationType = (byte) 1;//MainConstant.APPLICATION_TYPE_SS
        } else if (lowerCase.endsWith(MainConstant.FILE_TYPE_PPT) || lowerCase.endsWith(MainConstant.FILE_TYPE_PPTX) || lowerCase.endsWith(MainConstant.FILE_TYPE_POT) || lowerCase.endsWith(MainConstant.FILE_TYPE_PPTM) || lowerCase.endsWith(MainConstant.FILE_TYPE_POTX) || lowerCase.endsWith(MainConstant.FILE_TYPE_POTM) || lowerCase.endsWith(MainConstant.FILE_TYPE_PPS)) {
            this.applicationType = (byte) 2;
        } else if (lowerCase.endsWith("pdf")) {
            this.applicationType = (byte) 3;
        } else {
            this.applicationType = (byte) 0;//MainConstant.APPLICATION_TYPE_WP
        }
        boolean isSupport = FileKit.instance().isSupport(lowerCase);
        if (lowerCase.endsWith(MainConstant.FILE_TYPE_TXT) || !isSupport) {
            TXTKit.instance().readText(this, this.handler, filePath);
        } else {
            new FileReaderThread(this, this.handler, filePath, null).start();
        }
        return true;
    }

    @Override
    public void actionEvent(int actionID, final Object obj) {
        if (actionID == 23 && this.reader != null) { //EventConstant.SYS_READER_FINISH_ID
            IControl iControl = this.appControl;
            if (iControl != null) {
                iControl.actionEvent(actionID, obj);
            }
            this.reader.dispose();
            this.reader = null;
        }
        IMainFrame iMainFrame = this.frame;
        if (iMainFrame == null && iMainFrame.doActionEvent(actionID, obj)) {
            return;
        }
        if (iMainFrame != null && !iMainFrame.doActionEvent(actionID, obj)) {
            if (actionID == -268435456) { //EventConstant.TEST_REPAINT_ID
                getView().postInvalidate();
            } else if (actionID == 0) { //MainConstant.HANDLER_MESSAGE_SUCCESS
                try {
                    Message message = new Message();
                    message.obj = obj;
                    this.reader.dispose();
                    message.what = 0; //MainConstant.HANDLER_MESSAGE_SUCCESS
                    this.handler.handleMessage(message);
                } catch (Throwable th) {
                    this.sysKit.getErrorKit().writerLog(th);
                }
            } else if (actionID == 26) { //EventConstant.SYS_SET_PROGRESS_BAR_ID
                Handler handler = this.handler;
                if (handler != null) {
                    handler.post(() -> {
                        if (!MainControl.this.isDispose) {
                            MainControl.this.frame.showProgressBar((Boolean) obj);
                        }
                    });
                }
            } else if (actionID == 536870919) { //EventConstant.APP_CONTENT_SELECTED
                this.appControl.actionEvent(actionID, obj);
                this.frame.updateToolsbarStatus();
            } else if (actionID == 536870921) { //EventConstant.APP_ABORT_READING:
                IReader iReader = this.reader;
                if (iReader != null) {
                    iReader.abortReader();
                }
            } else if (actionID == 17) { //EventConstant.SYS_SHOW_TOOLTIP
                if (obj != null) {
                    if ((obj instanceof String)) {
                        try {
                            this.toast.setText((String) obj);
                            this.toast.setGravity(17, 0, 0);
                            this.toast.show();
                        } catch (Exception e) {
                        }
                    }
                }
            } else if (actionID == 18) { //EventConstant.SYS_CLOSE_TOOLTIP
                this.toast.cancel();
            } else if (actionID == 23) { //EventConstant.SYS_READER_FINISH_ID
                if (this.handler != null) {
                    this.handler.post(() -> {
                        if (!MainControl.this.isDispose) {
                            MainControl.this.frame.showProgressBar(false);
                        }
                    });
                }
            } else if (actionID == 24) { //EventConstant.SYS_START_BACK_READER_ID
                if (this.handler != null) {
                    this.handler.post(() -> {
                        if (!MainControl.this.isDispose) {
                            MainControl.this.frame.showProgressBar(true);
                        }
                    });
                }
            } else if (actionID == 117440512) { //EventConstant.TXT_DIALOG_FINISH_ID
                TXTKit.instance().reopenFile(this, this.handler, this.filePath, (String) obj);
            } else if (actionID == 117440513) { // EventConstant.TXT_RE_OPEN_ID
                String[] strArr = (String[]) obj;
                if (strArr.length == 2) {
                    this.filePath = strArr[0];
                    this.applicationType = (byte) 0;
                    TXTKit.instance().reopenFile(this, this.handler, this.filePath, strArr[1]);
                }
            } else {
                IControl iControl2 = this.appControl;
                if (this.appControl != null) {
                    iControl2.actionEvent(actionID, obj);
                }
            }
        }
    }

    @Override
    public IFind getFind() {
        return this.appControl.getFind();
    }

    @Override
    public Object getActionValue(int actionID, Object obj) {
        if (actionID == 1) { //EventConstant.SYS_FILE_PATH_ID
            return this.filePath;
        }
        IControl iControl = this.appControl;
        if (iControl == null) {
            return null;
        }
        if (actionID != 536870928 //EventConstant.APP_THUMBNAIL_ID
                && actionID != 805306371 //EventConstant.WP_PAGE_TO_IMAGE
                && actionID != 536870931 //EventConstant.APP_PAGE_AREA_TO_IMAGE
                && actionID != 1342177283 //EventConstant.PG_SLIDE_TO_IMAGE
                && actionID != 1358954506 //EventConstant.PG_SLIDESHOW_SLIDESHOW_TO_IMAGE
        ) {
            return iControl.getActionValue(actionID, obj);
        }
        boolean isDrawPictrue = PictureKit.instance().isDrawPictrue();
        boolean isThumbnail = this.frame.isThumbnail();
        PictureKit.instance().setDrawPictrue(true);
        if (actionID == 536870928) {//EventConstant.APP_THUMBNAIL_ID
            this.frame.setThumbnail(true);
        }
        Object actionValue = this.appControl.getActionValue(actionID, obj);
        if (actionID == 536870928) {//EventConstant.APP_THUMBNAIL_ID
            this.frame.setThumbnail(isThumbnail);
        }
        PictureKit.instance().setDrawPictrue(isDrawPictrue);
        return actionValue;
    }

    @Override
    public View getView() {
        if (this.appControl == null)
            return null;
        return this.appControl.getView();
    }

    @Override
    public boolean isAutoTest() {
        return this.isAutoTest;
    }

//    @Override
//    public IOfficeToPicture getOfficeToPicture() {
//        return null;
//    }

    @Override
    public IMainFrame getMainFrame() {
        return this.frame;
    }

    @Override
    public Activity getActivity() {
        return this.frame.getActivity();
    }

//    @Override
//    public IOfficeToPicture getOfficeToPicture() {
//        return this.officeToPicture;
//    }

    @Override
    public ICustomDialog getCustomDialog() {
        return this.customDialog;
    }

    @Override
    public ISlideShow getSlideShow() {
        return this.slideShow;
    }

    @Override
    public IReader getReader() {
        return this.reader;
    }

    @Override
    public byte getApplicationType() {
        return this.applicationType;
    }

//    public void setOffictToPicture(IOfficeToPicture opt) {
//        this.officeToPicture = opt;
//    }

    public void setCustomDialog(ICustomDialog dlg) {
        this.customDialog = dlg;
    }

    public void setSlideShow(ISlideShow slideshow) {
        this.slideShow = slideshow;
    }

    @Override
    public SysKit getSysKit() {
        return this.sysKit;
    }

    @Override
    public int getCurrentViewIndex() {
        if (appControl == null) {
            return 1;
        } else {
            return this.appControl.getCurrentViewIndex();
        }
    }

    public Bitmap getCurrentWordTxtPageBitmap(int pageNo) {
        Word wordView = (Word) appControl.getView();

        return wordView.pageToImage(pageNo);
    }
////

    public Bitmap getCurrentPptPageBitmap(int pageNo) {

        Presentation pptView = (Presentation) appControl.getView();

        return pptView.slideToImage(pageNo);
    }

    @Override
    public void dispose() {
        this.isDispose = true;
        IControl iControl = this.appControl;
        if (iControl != null) {
            iControl.dispose();
            this.appControl = null;
        }
        IReader iReader = this.reader;
        if (iReader != null) {
            iReader.dispose();
            this.reader = null;
        }
//        IOfficeToPicture iOfficeToPicture = this.officeToPicture;
//        if (iOfficeToPicture != null) {
//            iOfficeToPicture.dispose();
//            this.officeToPicture = null;
//        }
        /*ProgressDialog progressDialog = this.progressDialog;
        if (progressDialog != null) {
            progressDialog.dismiss();
            this.progressDialog = null;
        }*/
        if (this.customDialog != null) {
            this.customDialog = null;
        }
        if (this.slideShow != null) {
            this.slideShow = null;
        }
        this.frame = null;
        if (this.handler != null) {
            this.handler.removeCallbacksAndMessages(null);
            this.handler = null;
        }
//        AUncaughtExceptionHandler aUncaughtExceptionHandler = this.uncaught;
//        if (aUncaughtExceptionHandler != null) {
//            aUncaughtExceptionHandler.dispose();
//            this.uncaught = null;
//        }
        this.onKeyListener = null;
        this.toast = null;
        this.filePath = null;
        System.gc();
        if (this.sysKit != null) {
            this.sysKit.dispose();
        }
    }

    public void jumpToWordPage(int index) {
        Word view = (Word) appControl.getView();
        view.getCurrentPageNumber();
        view.showPage(index, EventConstant.WP_SHOW_PAGE);
    }

    public void changeOrientation() {
        if (appControl != null)
            appControl.actionEvent(EventConstant.WP_PRINT_MODE, null);
    }

    public void scrollToWordTxtPage(int pageNo) {
        Word wordView = (Word) appControl.getView();

        wordView.showPage(pageNo, EventConstant.WP_SHOW_PAGE);
    }

    public int getCurrentWordTxtPages() {
        Word wordView = (Word) appControl.getView();

        return wordView.getPageCount();
    }

    public Bitmap getCurrentWordTxtPageBitmap() {
        if (appControl == null)
            return null;

        try {
            Word wordView = (Word) appControl.getView();

            return wordView.pageToImage(wordView.getCurrentPageNumber());

        } catch (Exception e) {
            Log.e("none", "getCurrentWordTxtPageBitmap: "+e);
        }
        return null;
    }

    ////
    public void scrollToPPTPage(int pageNo) {

        Presentation pptView = (Presentation) appControl.getView();

        pptView.showSlide(pageNo, false);
    }

    public int getCurrentPptPages() {

        Presentation pptView = (Presentation) appControl.getView();

        return pptView.getSlideCount();
    }

    public Bitmap getCurrentPptPageBitmap() {
        if (appControl == null)
            return null;

        try {
            Presentation pptView = (Presentation) appControl.getView();
            if (pptView == null)
                return null;

            if (pptView.getCurrentIndex() < 0) {
                return null;
            } else {
                return pptView.slideToImage(pptView.getCurrentIndex() + 1);
            }
        } catch (Exception e) {
            Log.e("none", "getCurrentPptPageBitmap: "+e);
        }
        return null;
    }

}