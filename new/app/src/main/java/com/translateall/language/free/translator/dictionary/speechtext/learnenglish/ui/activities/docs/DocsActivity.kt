package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.docs

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.ahmadullahpk.alldocumentreader.xs.constant.EventConstant
import com.ahmadullahpk.alldocumentreader.xs.constant.MainConstant
import com.ahmadullahpk.alldocumentreader.xs.pg.control.Presentation
import com.ahmadullahpk.alldocumentreader.xs.system.IMainFrame
import com.ahmadullahpk.alldocumentreader.xs.system.MainControl
import com.ahmadullahpk.alldocumentreader.xs.wp.control.Word
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.hm.admanagerx.isAppOpenAdShow
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.R
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.databinding.ActivityDocsBinding
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.BaseActivity
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.dialogs.GoToDialog
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.DOCS_FILE_TYPE
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.PPT_FILE_TYPE
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.gone
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.isDoubleClick
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.launchInputScreen
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.showToast
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.visible
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.Locale

class DocsActivity : BaseActivity(), IMainFrame {
    private lateinit var binding: ActivityDocsBinding
    private var appFrame: LinearLayout? = null
    private var applicationType = -1
    private var bg: Any = -7829368
    private var control: MainControl? = null
    private var fileName: String? = null
    private var filePath: String? = null
    private var fileType: String = DOCS_FILE_TYPE
    private var isDispose = false
    private var isThumbnail = false
    private var writeLog = true
    private lateinit var jumpToDialog: GoToDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDocsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ Android 15+ edge-to-edge setup (only once, here)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ViewCompat.setOnApplyWindowInsetsListener(requireNotNull(binding).root) { view, windowInsets ->
                val systemBarInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
                val imeInsets = windowInsets.getInsets(WindowInsetsCompat.Type.ime())

                // Apply padding to your view using KTX extension
                view.updatePadding(
                    left = systemBarInsets.left,
                    top = systemBarInsets.top,
                    right = systemBarInsets.right,
                    bottom = imeInsets.bottom.coerceAtLeast(systemBarInsets.bottom)
                )

                // Consume the insets if you've handled them
                WindowInsetsCompat.CONSUMED
            }
        }

        init()
        setListeners()
    }

    private fun init() {
        binding.progressLayout.visible()
        control = MainControl(this)
        appFrame = findViewById(R.id.frameLL)
        if (intent != null) {
            fileType = intent.getStringExtra("fileType") ?: DOCS_FILE_TYPE
            fileName = intent.getStringExtra("name")
            val tempPath = intent.getStringExtra("path")
            tempPath?.let { copyFile(it) } ?: finish()
            binding.titleTV.setText(fileName)
           isAppOpenAdShow(false)
        }
    }

    private fun setListeners() {
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (isDoubleClick()) {
                        backPressed()
                    }
                }
            })

        binding.backIV.setOnClickListener {
            if (isDoubleClick()) {
                backPressed()
            }
        }

        binding.progressLayout.setOnClickListener {}

        binding.jumpIV.setOnClickListener {
            if (isDoubleClick()) {
                setJumpLimit()
                jumpToDialog?.let {
                    if (!it.isVisible && !it.isAdded)
                        it.show(supportFragmentManager, "jump_page_dialog")
                }
            }
        }

        binding.getTextBtn.setOnClickListener {
            if (isDoubleClick()) {
                binding.progressLayout.visible()
                if (fileType == PPT_FILE_TYPE) {
                    val bitmap = control?.currentPptPageBitmap
                    if (bitmap == null) {
                        showToast(getString(R.string.something_went_wrong), 0)
                        binding.progressLayout.gone()
                    } else {
                        extractText(
                            bitmap,
                            resultSuccess = { result ->
                                result?.let { resultText ->
                                    launchInputScreen(resultText, "")
                                    finish()
                                }
                            },
                            onErrorDetected = { error ->
                                showToast(error, 0)
                                binding.progressLayout.gone()
                            })
                    }
                } else {
                    val bitmap = control?.currentWordTxtPageBitmap
                    if (bitmap == null) {
                        showToast(getString(R.string.something_went_wrong), 0)
                        binding.progressLayout.gone()
                    } else {
                        extractText(
                            bitmap,
                            resultSuccess = { result ->
                                result?.let { resultText ->
                                    launchInputScreen(resultText, "")
                                    finish()
                                }
                            },
                            onErrorDetected = { error ->
                                showToast(error, 0)
                                binding.progressLayout.gone()
                            })
                    }
                }
            }
        }

    }

    private fun extractText(
        bitmap: Bitmap,
        resultSuccess: (String?) -> Unit,
        onErrorDetected: (String) -> Unit,
    ) {
        kotlin.runCatching {
            val image = InputImage.fromBitmap(bitmap, 0)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            recognizer.process(image)
                .addOnSuccessListener { mSuccess ->
                    mSuccess.let { result ->
                        var originalStr = result.text
                        originalStr = originalStr.replace("\n", " ")
                        resultSuccess.invoke(originalStr)
                    }
                }
                .addOnFailureListener {
                    it.localizedMessage?.let { e1 -> onErrorDetected.invoke(e1) }
                }
        }.onFailure {
            it.localizedMessage?.let { e2 -> onErrorDetected.invoke(e2) }

        }

    }

    private fun copyFile(uri: String) {
        try {
            CoroutineScope(Dispatchers.IO).launch {
                val myUri = Uri.parse(uri)
//            val sourceFile = File(myUri)
//            if (sourceFile == null || !sourceFile.exists()) {
//                return
//            }

                val inputStream: InputStream? = contentResolver.openInputStream(myUri)
//        val fileName = getFileName(uri)
                val destFileName = fileName ?: ""
                val destinationFile = File(this@DocsActivity.filesDir, destFileName)

                val outputStream: OutputStream = FileOutputStream(destinationFile)
                inputStream?.let {
                    copyFileContent(inputStream, outputStream)
                }
                inputStream?.close()
                outputStream.close()
                filePath = destinationFile.absolutePath
                withContext(Dispatchers.Main) {
                    binding.progressLayout.gone()
                    setViews()
                }
            }
        } catch (e: Exception) {
//            Log.e("none", "copyFile:e= ${e.message}")
            finish()
        } catch (ex: IOException) {
//            Log.e("none", "copyFile:ex= ${ex.message}")
            finish()
        }

    }

    private fun setViews() {
//        Log.e("none", "setViews: called")
        setJumpDialog()
        createView()
        control?.let { ctrl ->
            ctrl.openFile(filePath)
            /*ctrl.setOffictToPicture(object : IOfficeToPicture {
                override fun dispose() {}
                override fun getModeType(): Byte {
                    return 1
                }

                override fun isZoom(): Boolean {
                    return false
                }

                override fun setModeType(b: Byte) {}

                //changed return type from Bitmap to Bitmap? to accustom kotlin language
                override fun getBitmap(i: Int, i2: Int): Bitmap? {
                    return if (i != 0 && i2 != 0) {
                        val bitmap2 = bitmap
                        if (!(bitmap2 != null && bitmap2.width == i && bitmap?.height == i2)) {
                            val bitmap3 = bitmap
                            if (bitmap3 != null)
                                bitmap3.recycle()
                            bitmap = Bitmap.createBitmap(i, i2, Bitmap.Config.ARGB_8888)
                        }
                        bitmap
                    } else {
                        null
                    }
                }

                override fun callBack(bitmap: Bitmap) {}

            })*/
        }
    }

    private fun setJumpDialog() {
        jumpToDialog = GoToDialog.newInstance()
        jumpToDialog.initListener(object : GoToDialog.GoToPageListener {

            override fun pageNumber(num: Int) {
//                showToast(num.toString(), 0)
                jumpToPage(num - 1)
            }

        })
    }

    private fun jumpToPage(index: Int) {
        if (fileType == DOCS_FILE_TYPE) {
            control?.jumpToWordPage(index)
        } else {
            (control?.view as Presentation).showSlide(index, false)
        }
    }

    @Throws(IOException::class)
    fun copyFileContent(input: InputStream, output: OutputStream?) {
//        var count: Long = 0
        var n: Int
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        while (-1 != input.read(buffer).also { n = it }) {
            output!!.write(buffer, 0, n)
//            count += n.toLong()
        }
    }

    private fun createView() {
        val lowerCase = filePath?.lowercase(Locale.getDefault())
        lowerCase?.let {
            if (lowerCase.endsWith(MainConstant.FILE_TYPE_DOC) ||
                lowerCase.endsWith(MainConstant.FILE_TYPE_DOCX) ||
                lowerCase.endsWith(MainConstant.FILE_TYPE_TXT) ||
                lowerCase.endsWith(MainConstant.FILE_TYPE_DOT) ||
                lowerCase.endsWith(MainConstant.FILE_TYPE_DOTX) ||
                lowerCase.endsWith(MainConstant.FILE_TYPE_DOTM)
            ) {
                applicationType = 0
            } else if (lowerCase.endsWith(MainConstant.FILE_TYPE_XLS) ||
                lowerCase.endsWith(MainConstant.FILE_TYPE_XLSX) ||
                lowerCase.endsWith(MainConstant.FILE_TYPE_XLT) ||
                lowerCase.endsWith(MainConstant.FILE_TYPE_XLTX) ||
                lowerCase.endsWith(MainConstant.FILE_TYPE_XLTM) ||
                lowerCase.endsWith(MainConstant.FILE_TYPE_XLSM)
            ) {
                applicationType = 1
            } else if (lowerCase.endsWith(MainConstant.FILE_TYPE_PPT) ||
                lowerCase.endsWith(MainConstant.FILE_TYPE_PPTX) ||
                lowerCase.endsWith(MainConstant.FILE_TYPE_POT) ||
                lowerCase.endsWith(MainConstant.FILE_TYPE_PPTM) ||
                lowerCase.endsWith(MainConstant.FILE_TYPE_POTX) ||
                lowerCase.endsWith(MainConstant.FILE_TYPE_POTM)
            ) {
                applicationType = 2
            } else if (lowerCase.endsWith("pdf")) {
                applicationType = 3
            } else {
                applicationType = 0
            }
        }

    }

    override fun showLoader() {
        runOnUiThread {
            binding.progressLayout.visible()
        }
    }

    override fun dismissLoader() {
        runOnUiThread {
            binding.progressLayout.gone()
        }
    }

    override fun getActivity(): Activity {
        return this
    }

    override fun doActionEvent(actionID: Int, obj: Any?): Boolean {
        if (actionID == 0) {
            onBackPressed()
        } else if (actionID != 15) {
            if (actionID == 20) {
                updateToolsbarStatus()
            } else if (actionID == 25) {
                title = obj as String?
            } else if (actionID != 268435464) {
                if (actionID == 536870913) {
                    fileShare()
                } else if (actionID == 788529152) {
                    val trim = (obj as String).trim { it <= ' ' }
                    if (trim.length <= 0 || control?.find?.find(trim) == false) {
                        setFindBackForwardState(false)
//                        showToast("DIALOG_FIND_NOT_FOUND", 0)
                    } else {
                        setFindBackForwardState(true)
                    }
                } else if (actionID != 1073741828) {
                    when (actionID) {
                        EventConstant.APP_DRAW_ID -> {
                            control?.getSysKit()?.calloutManager?.drawingMode = 1
                            appFrame?.post {
                                this.control?.actionEvent(
                                    EventConstant.APP_INIT_CALLOUTVIEW_ID,
                                    null
                                )
                            }
                        }

                        EventConstant.APP_BACK_ID ->
                            control?.getSysKit()?.calloutManager?.drawingMode = 0

                        EventConstant.APP_PEN_ID -> if (!(obj as Boolean)) {
                            control?.getSysKit()?.calloutManager?.drawingMode = 0
                        } else {
                            control?.getSysKit()?.calloutManager?.drawingMode = 1
                            appFrame?.post {
                                this.control?.actionEvent(
                                    EventConstant.APP_INIT_CALLOUTVIEW_ID,
                                    null
                                )
                            }
                        }

                        EventConstant.APP_ERASER_ID -> {
                            try {
                                if (!(obj as Boolean)) {
                                    control?.getSysKit()?.calloutManager?.drawingMode = 0
                                } else {
                                    control?.getSysKit()?.calloutManager?.drawingMode = 2
                                }
                            } catch (e: Exception) {
                                control?.getSysKit()?.errorKit?.writerLog(e)
                            }
                            return false
                        }

                        else -> return false
                    }
                }
            }
        }
        return true
    }

    override fun openFileFinish() {
        val view = View(applicationContext)
        view.setBackgroundColor(getColor(R.color.app_back_ground))
//        view.setBackgroundResource(R.color.app_back_ground)
        appFrame?.addView(control?.view, LinearLayout.LayoutParams(-1, -1))
        control?.changeOrientation()
        setJumpDialog()
    }

    private fun setJumpLimit() {
        val count = if (fileType == DOCS_FILE_TYPE) {
            (control?.view as? Word)?.pageCount
        } else {
            (control?.view as? Presentation)?.slideCount
        }
        count?.let {
            jumpToDialog.setPagesLimit(it)
        }
    }

    override fun updateToolsbarStatus() {}

    override fun setFindBackForwardState(state: Boolean) {}

    override fun getBottomBarHeight(): Int {
        return 0
    }

    override fun getTopBarHeight(): Int {
        return 0
    }

    override fun getAppName(): String {
        return getString(R.string.app_name)
    }

    override fun getTemporaryDirectory(): File {
        val externalFilesDir = getExternalFilesDir(null)
        return externalFilesDir ?: filesDir
    }

    override fun onEventMethod(
        v: View?,
        e1: MotionEvent?,
        e2: MotionEvent?,
        xValue: Float,
        yValue: Float,
        eventMethodType: Byte
    ): Boolean {
        return false
    }

    override fun isDrawPageNumber(): Boolean {
        return true
    }

    override fun isShowZoomingMsg(): Boolean {
        return false
    }

    override fun isPopUpErrorDlg(): Boolean {
        return true
    }

    override fun showErrorMessage() {

    }

    override fun isShowPasswordDlg(): Boolean {
        return true
    }

    override fun isShowProgressBar(): Boolean {
        return true
    }

    override fun isShowFindDlg(): Boolean {
        return true
    }

    override fun isShowTXTEncodeDlg(): Boolean {
        return true
    }

    override fun getTXTDefaultEncode(): String {
        return "GBK"
    }

    override fun isTouchZoom(): Boolean {
        return true
    }

    override fun isZoomAfterLayoutForWord(): Boolean {
        return true
    }

    override fun getWordDefaultView(): Byte {
        return 0
    }

    override fun getLocalString(resName: String): String {
        return resName
    }

    override fun changeZoom(zoom: Float) {

    }

    override fun changePage() {}

    override fun completeLayout() {}

    override fun error(errorCode: Int) {}

    override fun fullScreen(fullscreen: Boolean) {}

    override fun showProgressBar(visible: Boolean) {}

    override fun updateViewImages(viewList: MutableList<Int>?) {}

    override fun isChangePage(): Boolean {
        return true
    }

    override fun setWriteLog(saveLog: Boolean) {
        this.writeLog = saveLog
    }

    override fun isWriteLog(): Boolean {
        return this.writeLog
    }

    override fun setThumbnail(isThumbnail: Boolean) {
        this.isThumbnail = isThumbnail
    }

    override fun isThumbnail(): Boolean {
        return this.isThumbnail
    }

    override fun getViewBackground(): Any {
        return this.bg
    }

    override fun setIgnoreOriginalSize(ignoreOriginalSize: Boolean) {}

    override fun isIgnoreOriginalSize(): Boolean {
        return false
    }

    override fun getPageListViewMovingPosition(): Byte {
        return 0
    }

    override fun dispose() {
        isDispose = true

        val mainControl = control
        if (mainControl != null) {
            mainControl.dispose()
            control = null
        }
        val linearLayout = appFrame
        if (linearLayout != null) {
            val childCount = linearLayout.childCount
            for (i in 0 until childCount) {
                appFrame?.getChildAt(i)
            }
            appFrame = null
        }
        filePath?.let {
            val file = File(it)
            if (file.exists())
                file.delete()
        }

    }

    private fun fileShare() {
        val arrayList = ArrayList<Uri>()
        arrayList.add(Uri.fromFile(File(filePath)))
        val intent = Intent(Intent.ACTION_SEND_MULTIPLE)
        intent.putExtra(Intent.EXTRA_STREAM, arrayList)
        intent.setType("application/octet-stream")
        startActivity(Intent.createChooser(intent, "Share ..."))
    }

    private fun backPressed() {
        if (control?.reader != null) {
            control?.reader?.abortReader()
        }
        finish()
    }

    override fun onDestroy() {
        dispose()
        super.onDestroy()
    }

}