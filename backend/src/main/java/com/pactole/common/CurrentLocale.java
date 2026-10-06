package com.pactole.common;


import java.util.Locale;

import org.springframework.context.i18n.LocaleContextHolder;


public class CurrentLocale {
    private CurrentLocale() {}

    public static Locale getCurrentLocale() {
        return LocaleContextHolder.getLocale();
    }
    
}
