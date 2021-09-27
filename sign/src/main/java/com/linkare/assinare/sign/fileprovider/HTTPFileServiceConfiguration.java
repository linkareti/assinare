package com.linkare.assinare.sign.fileprovider;

public class HTTPFileServiceConfiguration {

    private final String getFileURLPrefix;
    private final String getSignedFileURLPrefix;
    private final String putFileURL;
    private final String cookieString;

    public HTTPFileServiceConfiguration(final String getFileURLPrefix, final String getSignedFileURLPrefix, final String putFileURL, final String cookieString) {
	this.getFileURLPrefix = getFileURLPrefix;
	this.getSignedFileURLPrefix = getSignedFileURLPrefix;
	this.putFileURL = putFileURL;
	this.cookieString = cookieString;
    }

    public String getGetFileURLPrefix() {
	return this.getFileURLPrefix;
    }

    public String getGetSignedFileURLPrefix() {
	return this.getSignedFileURLPrefix;
    }

    public String getPutFileURL() {
	return this.putFileURL;
    }

    public String getCookieString() {
	return this.cookieString;
    }
}
