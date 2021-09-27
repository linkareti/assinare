package com.linkare.assinare.server;

/**
 *
 * @author bnazare
 */
public class CMDException extends Exception {

    private static final long serialVersionUID = -3327632340140554459L;

    private final ErrorCode errorCode;

    public CMDException(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public CMDException(ErrorCode errorCode, String string) {
        super(errorCode.toString() + ": " + string);
        this.errorCode = errorCode;
    }

    public CMDException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.toString() + ": " + cause.toString(), cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

}
