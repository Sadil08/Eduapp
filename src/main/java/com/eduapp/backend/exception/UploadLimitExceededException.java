package com.eduapp.backend.exception;

public class UploadLimitExceededException extends RuntimeException {

    public UploadLimitExceededException(String message) {
        super(message);
    }

    public UploadLimitExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}
