package com.pactole.common;

import com.pactole.constants.CommonConstants;

import jakarta.servlet.http.HttpServletRequest;

public record  ClientInfo(String ip, String userAgent) {

    public static ClientInfo of(HttpServletRequest request) {
        String agent = request.getHeader("User-Agent");
         return new ClientInfo(truncate(request.getRemoteAddr(), CommonConstants.MAX_TRUNCATE_REMOTE),
            agent == null ? null : truncate(LogSanitizer.clean(agent), CommonConstants.MAX_TRUNCATE_LOG));
    }

    private static String truncate(String value , int max) {
        return value  == null || value .length() <= max ? value  : value.substring(0, max);
    }
    
}
