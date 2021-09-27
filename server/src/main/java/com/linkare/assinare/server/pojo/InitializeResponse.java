package com.linkare.assinare.server.pojo;

/**
 *
 * @author bnazare
 */
public class InitializeResponse {

    private final String processId;

    public InitializeResponse(String processId) {
        this.processId = processId;
    }

    /**
     * @return the processId
     */
    public String getProcessId() {
        return processId;
    }

}
