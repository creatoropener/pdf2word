package com.cometfile.pdf2docx.api;

public class ConversionException extends Exception {
    public ConversionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConversionException(String message) {
        super(message);
    }
}
