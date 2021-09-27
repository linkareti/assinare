package com.linkare.assinare.commons;

/**
 *
 * @author rvaz
 */
public class AssinareError extends Exception {
    
    public AssinareError() {
        super();
    }
    
    public AssinareError(String message) {
        super(message);
    }
    
     public AssinareError(Throwable cause) {
        super("Ocorreu um erro interno.", cause);
    }
     
     public AssinareError(String message, Throwable cause) {
        super(message, cause);
    }
    
}
