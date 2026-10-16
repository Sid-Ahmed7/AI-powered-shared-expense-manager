package com.pactole.common;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@Component
public class Messages {

    private final MessageSource source;

    public Messages(MessageSource source) {
        this.source = source;
    }

    public String get(String key, Object... args) {
        return source.getMessage(key, args, CurrentLocale.getCurrentLocale());
    }
}
