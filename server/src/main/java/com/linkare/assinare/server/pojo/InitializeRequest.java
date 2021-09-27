package com.linkare.assinare.server.pojo;

import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotEmpty;

/**
 *
 * @author bnazare
 */
public class InitializeRequest {

    @NotEmpty
    private List<String> docNames;

    @NotEmpty
    private String userId;

    @NotEmpty
    private String userPin;

    private PDFSignatureOptions signatureOptions;

    private Map<String, String> docParams;

    /**
     * @return the docNames
     */
    public List<String> getDocNames() {
        return docNames;
    }

    /**
     * @param docNames the docNames to set
     */
    public void setDocNames(List<String> docNames) {
        this.docNames = docNames;
    }

    /**
     * @return the userId
     */
    public String getUserId() {
        return userId;
    }

    /**
     * @param userId the userId to set
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * @return the userPin
     */
    public String getUserPin() {
        return userPin;
    }

    /**
     * @param userPin the userPin to set
     */
    public void setUserPin(String userPin) {
        this.userPin = userPin;
    }

    /**
     * @return the signatureOptions
     */
    public PDFSignatureOptions getSignatureOptions() {
        return signatureOptions;
    }

    /**
     * @param signatureOptions the signatureOptions to set
     */
    public void setSignatureOptions(PDFSignatureOptions signatureOptions) {
        this.signatureOptions = signatureOptions;
    }

    /**
     * @return the docParams
     */
    public Map<String, String> getDocParams() {
        return docParams;
    }

    /**
     * @param docParams the docParams to set
     */
    public void setDocParams(Map<String, String> docParams) {
        this.docParams = docParams;
    }

}
