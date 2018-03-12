package com.ef.service;

public class EfException extends Exception {
    public EfException() {
    }
    
    public EfException(String message) {
        super(message);
    }
    
    public EfException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public EfException(Throwable cause) {
        super(cause);
    }
    
    public EfException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
