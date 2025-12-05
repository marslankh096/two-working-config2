package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils;

import android.hardware.Camera;

import java.util.List;

public class CameraConstants {

    public static Camera.Parameters getSupportedParameters(Camera.Parameters mParameters, int width) {
        Camera.Parameters parameters = mParameters;
        List supportedPictureSizes = parameters.getSupportedPictureSizes();
        for (int i = 0; i < supportedPictureSizes.size() - 1; i++) {
            int i2 = 0;
            while (i2 < (supportedPictureSizes.size() - 1) - i) {
                int i3 = i2 + 1;
                if (((Camera.Size) supportedPictureSizes.get(i2)).height > ((Camera.Size) supportedPictureSizes.get(i3)).height) {
                    Camera.Size size = (Camera.Size) supportedPictureSizes.get(i2);
                    supportedPictureSizes.set(i2, supportedPictureSizes.get(i3));
                    supportedPictureSizes.set(i3, size);
                }
                i2 = i3;
            }
        }
        if (supportedPictureSizes.size() > 1) {
            int i4 = 0;
            while (true) {
                if (i4 >= supportedPictureSizes.size()) {
                    break;
                } else if (((Camera.Size) supportedPictureSizes.get(i4)).height >= width) {
                    parameters.setPictureSize(((Camera.Size) supportedPictureSizes.get(i4)).width, ((Camera.Size) supportedPictureSizes.get(i4)).height);
                    break;
                } else {
                    i4++;
                }
            }
        }
        return parameters;
    }

}