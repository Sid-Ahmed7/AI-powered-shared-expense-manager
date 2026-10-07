package com.pactole.common;

import java.text.Normalizer;

import com.pactole.constants.CommonConstants;

public final class TextMatching {

    private TextMatching() {
    }

    public static String normalize(String text) {
        if (text == null) return "";
        String normalizerForm = Normalizer.normalize(text.toLowerCase(), Normalizer.Form.NFD);
        return normalizerForm.replaceAll(CommonConstants.NORMALIZE_FORM, "");
    }
}
