package com.linkare.assinare.sign;

import com.linkare.assinare.commons.AssinareException;

/**
 *
 * @author bnazare
 */
public class DocumentParseException extends AssinareException {
    
    public DocumentParseException() {
        super();
    }
    
    public DocumentParseException(String message) {
        super(message);
    }
    
     public DocumentParseException(Throwable cause) {
        super(cause);
    }
     
     public DocumentParseException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
