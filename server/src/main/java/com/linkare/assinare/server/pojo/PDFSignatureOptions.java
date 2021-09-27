package com.linkare.assinare.server.pojo;

import java.net.URL;

import com.linkare.assinare.sign.SignatureRenderingMode;
import com.linkare.assinare.sign.pdf.PDFSignatureFields;

/**
 *
 * @author bnazare
 */
public class PDFSignatureOptions {

    private String contact;

    private String location;

    private String reason;

    private Double percentX;

    private Double percentY;

    private Integer width;

    private Integer height;

    private Integer pageNumber;

    private SignatureRenderingMode sigRenderingMode;

    private URL logoFileURL;

    private String fieldName;

    private String tsaUrl;

    private Boolean doLTV;

    /**
     * @return the contact
     */
    public String getContact() {
        return contact;
    }

    /**
     * @param contact the contact to set
     */
    public void setContact(String contact) {
        this.contact = contact;
    }

    /**
     * @return the location
     */
    public String getLocation() {
        return location;
    }

    /**
     * @param location the location to set
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * @return the reason
     */
    public String getReason() {
        return reason;
    }

    /**
     * @param reason the reason to set
     */
    public void setReason(String reason) {
        this.reason = reason;
    }

    /**
     * @return the percentX
     */
    public Double getPercentX() {
        return percentX;
    }

    /**
     * @param percentX the percentX to set
     */
    public void setPercentX(Double percentX) {
        this.percentX = percentX;
    }

    /**
     * @return the percentY
     */
    public Double getPercentY() {
        return percentY;
    }

    /**
     * @param percentY the percentY to set
     */
    public void setPercentY(Double percentY) {
        this.percentY = percentY;
    }

    /**
     * @return the width
     */
    public Integer getWidth() {
        return width;
    }

    /**
     * @param width the width to set
     */
    public void setWidth(Integer width) {
        this.width = width;
    }

    /**
     * @return the height
     */
    public Integer getHeight() {
        return height;
    }

    /**
     * @param height the height to set
     */
    public void setHeight(Integer height) {
        this.height = height;
    }

    /**
     * @return the pageNumber
     */
    public Integer getPageNumber() {
        return pageNumber;
    }

    /**
     * @param pageNumber the pageNumber to set
     */
    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }

    /**
     * @return the sigRenderingMode
     */
    public SignatureRenderingMode getSigRenderingMode() {
        return sigRenderingMode;
    }

    /**
     * @param sigRenderingMode the sigRenderingMode to set
     */
    public void setSigRenderingMode(SignatureRenderingMode sigRenderingMode) {
        this.sigRenderingMode = sigRenderingMode;
    }

    /**
     * @return the logoFileURL
     */
    public URL getLogoFileURL() {
        return logoFileURL;
    }

    /**
     * @param logoFileURL the logoFileURL to set
     */
    public void setLogoFileURL(URL logoFileURL) {
        this.logoFileURL = logoFileURL;
    }

    /**
     * @return the fieldName
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * @param fieldName the fieldName to set
     */
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    /**
     * @return the tsaUrl
     */
    public String getTsaUrl() {
        return tsaUrl;
    }

    /**
     * @param tsaUrl the tsaUrl to set
     */
    public void setTsaUrl(String tsaUrl) {
        this.tsaUrl = tsaUrl;
    }

    /**
     * @return the doLTV
     */
    public Boolean getDoLTV() {
        return doLTV;
    }

    /**
     * @param doLTV the doLTV to set
     */
    public void setDoLTV(Boolean doLTV) {
        this.doLTV = doLTV;
    }

    public PDFSignatureFields toPDFSignatureFields() {
        return new PDFSignatureFields(getContact(), getLocation(), getReason(), getPercentX(), getPercentY(), getPageNumber(), getWidth(), getHeight(), getSigRenderingMode(), getFieldName(), getTsaUrl(), getDoLTV(), getLogoFileURL(), null);
    }

}
