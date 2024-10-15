package com.springboot.azureapp.exception;

public class DemoModelNotFoundException extends Exception{
    public DemoModelNotFoundException() {
        super();
    }

    public DemoModelNotFoundException(String message) {
        super(message);
    }

    public DemoModelNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public DemoModelNotFoundException(Throwable cause) {
        super(cause);
    }

    protected DemoModelNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
