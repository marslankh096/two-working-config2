package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.widgets;

import android.content.Context;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

public class CustomCamView extends SurfaceView implements Callback {
    private final SurfaceHolder surfaceHolder = getHolder();
    private Camera camera;

    public CustomCamView(Context context) {
        super(context);

    }

    public CustomCamView(Context context, Camera camera) {
        super(context);
        this.camera = camera;
        this.surfaceHolder.addCallback(this);
        this.surfaceHolder.setType(3);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
    }
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {
            this.camera.setDisplayOrientation(90);
            this.camera.setPreviewDisplay(surfaceHolder);
            this.camera.startPreview();
        } catch (Exception unused) {
        }
    }
    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
        if (surfaceHolder.getSurface() != null) {
            try {
                this.camera.cancelAutoFocus();
                this.camera.stopPreview();
                this.camera.setPreviewDisplay(this.surfaceHolder);
                this.camera.startPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
