package com.linkare.assinare.commons;

/**
 *
 * @author rvaz
 */
public class AssinareException extends Exception {

    private static final long serialVersionUID = -7312989497816179997L;

    public AssinareException() {
        super();
    }
    
    public AssinareException(String message) {
        super(message);
    }
    
     public AssinareException(Throwable cause) {
        super(cause);
    }
     
     public AssinareException(String message, Throwable cause) {
        super(message, cause);
    }
}