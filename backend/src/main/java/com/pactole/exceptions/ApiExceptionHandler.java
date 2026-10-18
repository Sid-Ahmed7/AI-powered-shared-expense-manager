package com.pactole.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.pactole.common.CurrentLocale;

import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    private final MessageSource messages;

    public ApiExceptionHandler(MessageSource messages) {
        this.messages = messages;
    }

    @ExceptionHandler(Errors.Validation.class)
    ProblemDetail validationError(Errors.Validation e) {
        String detail = messages.getMessage(e.getMessageKey(), e.getArgs(), e.getMessageKey(), CurrentLocale.getCurrentLocale());
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
    }

    @ExceptionHandler(Errors.NotFound.class)
    ProblemDetail notFound(Errors.NotFound e) {
        String detail = messages.getMessage(e.getMessageKey(), e.getArgs(), e.getMessageKey(), CurrentLocale.getCurrentLocale());
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, detail);
    }

    @ExceptionHandler(Errors.Unauthorized.class)
    ProblemDetail unauthorized(Errors.Unauthorized e) {
        String detail = messages.getMessage(e.getMessageKey(), e.getArgs(), e.getMessageKey(), CurrentLocale.getCurrentLocale());
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, detail);
    }

    @ExceptionHandler(Errors.Forbidden.class)
    ProblemDetail forbidden(Errors.Forbidden e) {
        String detail = messages.getMessage(e.getMessageKey(), e.getArgs(), e.getMessageKey(), CurrentLocale.getCurrentLocale());
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, detail);
    }

    @ExceptionHandler(Errors.Conflict.class)
    ResponseEntity<ProblemDetail> conflict(Errors.Conflict e) {
        String detail = messages.getMessage(e.getMessageKey(), e.getArgs(), e.getMessageKey(), CurrentLocale.getCurrentLocale());
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, detail);
        if (e.getCode() != null) pd.setProperty("code", e.getCode());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(pd);
    }

    @ExceptionHandler(Errors.AiUnavailable.class)
    ProblemDetail aiUnavailable(Errors.AiUnavailable e) {
        String detail = messages.getMessage(e.getMessageKey(), e.getArgs(), e.getMessage(), CurrentLocale.getCurrentLocale());
        return ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, detail);
    }

    @ExceptionHandler(Errors.TooManyRequests.class)
    ResponseEntity<ProblemDetail> tooManyRequests(Errors.TooManyRequests e) {
        String detail = messages.getMessage(e.getMessageKey(), e.getArgs(), e.getMessage(), CurrentLocale.getCurrentLocale());
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.TOO_MANY_REQUESTS, detail);
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.RETRY_AFTER, String.valueOf(e.retryAfterSeconds()));
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).headers(headers).body(pd);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    ProblemDetail optimisticLock(ObjectOptimisticLockingFailureException e) {
        String detail = messages.getMessage("expense.optimistic.lock", null, e.getMessage(), CurrentLocale.getCurrentLocale());
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, detail);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail badRequest(IllegalArgumentException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail validation(MethodArgumentNotValidException e) {
        String detail = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + " : " + f.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail internalError(Exception e) {
        log.error("Unexpected error", e);
        String detail = messages.getMessage("error.internal", null, "An unexpected error occurred", CurrentLocale.getCurrentLocale());
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, detail);
    }
}
