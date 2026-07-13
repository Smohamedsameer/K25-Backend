package com.FundRaise.F25.controller;



import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;

/* ─────────────────────────────────────────
   GLOBAL EXCEPTION HANDLER
   Spring Boot 3 suppresses exception messages from error
   responses by default, so failures showed up in the UI as a
   generic "Request failed" with no clue why. This makes sure
   every error response actually carries a useful message.
───────────────────────────────────────── */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<?> handleTooLarge(MaxUploadSizeExceededException ex) {
        return error(HttpStatus.PAYLOAD_TOO_LARGE,
                "That screenshot is too large — please upload an image under 20MB.");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleBadArgument(IllegalArgumentException ex) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneric(Exception ex) {
        ex.printStackTrace(); // still shows full stack trace in the backend console
        return error(HttpStatus.INTERNAL_SERVER_ERROR,
                "Something went wrong on the server: " + ex.getMessage());
    }

    private ResponseEntity<?> error(HttpStatus status, String message) {
        Map<String, String> body = new HashMap<>();
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}