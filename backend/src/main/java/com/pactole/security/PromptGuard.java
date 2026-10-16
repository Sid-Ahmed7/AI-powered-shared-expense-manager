package com.pactole.security;

import java.text.Normalizer;

import com.pactole.constants.CommonConstants;

import lombok.NoArgsConstructor;

@NoArgsConstructor 
public class PromptGuard {

    public static String clean(String input) {
        if(input == null) {
            return "";
        }

        String normalizedText = Normalizer.normalize(input, Normalizer.Form.NFKC);
        normalizedText = normalizedText.replaceAll(CommonConstants.FIRST_FORMAT_CONTROL, "");
        normalizedText = normalizedText.replaceAll(CommonConstants.SECOND_FORMAT_CONTROL, "");
        normalizedText = normalizedText.strip();
        return  normalizedText.length() > CommonConstants.MAX_LENGTH_PROMPT ? normalizedText.substring(0, CommonConstants.MAX_LENGTH_PROMPT) : normalizedText;
    }    
}
