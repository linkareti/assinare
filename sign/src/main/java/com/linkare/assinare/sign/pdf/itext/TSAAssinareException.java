package com.linkare.assinare.sign.pdf.itext;

import com.linkare.assinare.commons.AssinareException;

/**
 *
 * @author rvaz
 */
public class TSAAssinareException extends AssinareException {

    public TSAAssinareException(String message) {
        super(message);
    }

    public TSAAssinareException(String message, Throwable cause) {
        super(message, cause);
    }

    public TSAAssinareException(Throwable cause) {
        super(cause);
    }

}
