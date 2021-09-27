package com.linkare.assinare.server.pojo;

import javax.validation.constraints.NotNull;

/**
 *
 * @author bnazare
 */
public class FinalizeRequest {

    @NotNull
    private String processId;

    @NotNull
    private String userOtp;

    /**
     * @return the processId
     */
    public String getProcessId() {
        return processId;
    }

    /**
     * @param processId the processId to set
     */
    public void setProcessId(String processId) {
        this.processId = processId;
    }

    /**
     * @return the userOtp
     */
    public String getUserOtp() {
        return userOtp;
    }

    /**
     * @param userOtp the userOtp to set
     */
    public void setUserOtp(String userOtp) {
        this.userOtp = userOtp;
    }

}
