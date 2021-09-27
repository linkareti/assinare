package com.linkare.assinare.server.pojo;

import java.time.ZonedDateTime;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 *
 * @author bnazare
 */
public class GetHashParams {

    @NotEmpty
    private String docName;

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
