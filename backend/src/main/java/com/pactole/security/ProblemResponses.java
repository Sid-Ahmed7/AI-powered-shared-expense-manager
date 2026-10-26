package com.pactole.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pactole.constants.CommonConstants;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ProblemResponses {

    private final ObjectMapper objectMapper;

    public ProblemResponses(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void write(HttpServletResponse response, int status, String detail, String... headerPairs) throws IOException {
        for (int i = 0; i + 1 < headerPairs.length; i += 2) {
            response.setHeader(headerPairs[i], headerPairs[i + 1]);
        }
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding(CommonConstants.ENCODING);
        ProblemDetail problemDetail = ProblemDetail.forStatus(status);
        problemDetail.setDetail(detail);
        objectMapper.writeValue(response.getWriter(), problemDetail);
    }
}
