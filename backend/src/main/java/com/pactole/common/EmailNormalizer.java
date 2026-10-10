package com.pactole.common;

import java.util.Locale;

import lombok.NoArgsConstructor;

@NoArgsConstructor 
public class EmailNormalizer {
     public static String normalizeEmail(String email) {
        return email == null ? "" : email.strip().toLowerCase(Locale.ROOT);
    } 
    
}
