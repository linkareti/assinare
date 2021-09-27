package com.linkare.assinare.sign.fileprovider;

import com.linkare.assinare.commons.AssinareException;

/**
 *
 * @author bnazare
 */
public class FileAccessException extends AssinareException {

    /**
     * Creates a new instance of <code>FileAccessException</code> without detail
     * message.
     */
    public FileAccessException() {
    }

    /**
     * Constructs an instance of <code>FileAccessException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public FileAccessException(String msg) {
        super(msg);
    }

    public FileAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileAccessException(Throwable cause) {
        super(cause);
    }
    
}
