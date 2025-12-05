package com.ahmadullahpk.alldocumentreader.utils;

import android.text.InputFilter;
import android.text.Spanned;

public class NonZeroSingleDigitFilter implements InputFilter {

    @Override
    public CharSequence filter(CharSequence source, int start, int end,
                               Spanned dest, int dstart, int dend) {
        if (dest.length() == 0 && source.length() > 0 && source.charAt(0) == '0') {
            // Prevent entering '0' as the first character
            return "";
        }
        return null;
    }
}