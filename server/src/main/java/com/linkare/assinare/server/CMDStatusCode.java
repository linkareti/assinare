package com.linkare.assinare.server;

/**
 *
 * @author bnazare
 */
public enum CMDStatusCode {

    SUCCESS("200"),
    PIN_MISMATCH("801"),
    INVALID_OTP("802"),
    OTP_TIMEOUT("816"),
    INACTIVE_SIGNATURE("817"),
    GENERAL_ERROR("900");

    private final String code;

    private CMDStatusCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static CMDStatusCode forCode(String code) {
        for (CMDStatusCode statusCode : values()) {
            if (statusCode.matches(code)) {
                return statusCode;
            }
        }

        return null;
    }

    public boolean matches(String otherCode) {
        return code.equals(otherCode);
    }

}
