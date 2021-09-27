package com.linkare.assinare.sign.dss;

import eu.europa.esig.dss.model.DSSException;

/**
 *
 * @author bnazare
 */
public class TSPDSSException extends DSSException {

    public TSPDSSException() {
    }

    public TSPDSSException(String message) {
        super(message);
    }

    public TSPDSSException(Throwable cause) {
        super(cause);
    }

    public TSPDSSException(String message, Throwable cause) {
        super(message, cause);
    }

}
