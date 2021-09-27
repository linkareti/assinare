package com.linkare.assinare.server.pojo;

import java.time.ZonedDateTime;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 *
 * @author bnazare
 */
public class SignHashParams {

    @NotEmpty
    private String docName;

    @NotEmpty
    private byte[] signedHash;

    @NotEmpty
    private byte[] cert;

    @NotNull
    private ZonedDateTime date;

    /**
     * @return the docName
     */
    public String getDocName() {
        return docName;
    }

    /**
     * @param docName the docName to set
     */
    public void setDocName(String docName) {
        this.docName = docName;
    }

    /**
     * @return the signedHash
     */
    public byte[] getSignedHash() {
        return signedHash;
    }

    /**
     * @param signedHash the signedHash to set
     */
    public void setSignedHash(byte[] signedHash) {
        this.signedHash = signedHash;
    }

    /**
     * @return the cert
     */
    public byte[] getCert() {
        return cert;
    }

    /**
     * @param cert the cert to set
     */
    public void setCert(byte[] cert) {
        this.cert = cert;
    }

    /**
     * @return the date
     */
    public ZonedDateTime getDate() {
        return date;
    }

    /**
     * @param date the date to set
     */
    public void setDate(ZonedDateTime date) {
        this.date = date;
    }

}
