package com.linkare.assinare.sign.fileprovider;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpResponse;

public class HTTPAssinareException extends FileAccessException {

    private static final long serialVersionUID = 6140732341104249602L;

    private final String docName;
    private final String docUrl;
    private Map<String, List<String>> headerFields;
    private String responseMessage;
    private Integer statusCode;

    public HTTPAssinareException(final String docName, final String docUrl, final String message, final HttpResponse httpResponse) {
        this(docName, docUrl, message,
                httpResponse.getStatusLine().getStatusCode(),
                httpResponse.getStatusLine().getReasonPhrase(),
                convertHeaders(httpResponse.getAllHeaders()));
    }

    private static Map<String, List<String>> convertHeaders(final Header[] allHeaders) {
        final Map<String, List<String>> headerFields = new HashMap<>();
        for (Header header : allHeaders) {
            final List<String> valueList = new LinkedList<>();
            valueList.add(header.getValue());
            headerFields.put(header.getName(), valueList);
        }

        return headerFields;
    }

    public HTTPAssinareException(final String docName, final String docUrl, final String message, final Integer statusCode, final String responseMessage, final Map<String, List<String>> headerFields) {
        super(message);
        this.docName = docName;
        this.docUrl = docUrl;
        this.statusCode = statusCode;
        this.responseMessage = responseMessage;
        this.headerFields = headerFields;
    }

    public HTTPAssinareException(final String docName, final String docUrl, final String message, final Throwable cause) {
        super(message, cause);
        this.docName = docName;
        this.docUrl = docUrl;
    }

    public HTTPAssinareException(final String docName, final String docUrl, final Throwable cause) {
        super(cause);
        this.docName = docName;
        this.docUrl = docUrl;
    }

    public String getDocName() {
        return this.docName;
    }

    public String getDocUrl() {
        return docUrl;
    }

    public Map<String, List<String>> getHeaderFields() {
        return this.headerFields;
    }

    public String getResponseMessage() {
        return this.responseMessage;
    }

    public Integer getStatusCode() {
        return this.statusCode;
    }

    @Override
    public String toString() {
        if (statusCode != null) {
            return super.toString() + "[docName=" + docName + ", docUrl=" + docUrl + ", statusCode=" + statusCode + ", responseMessage=" + responseMessage + ", headerFields=" + headerFields + ']';
        } else {
            return super.toString() + "[docName=" + docName + ", docUrl=" + docUrl + ']';
        }
    }

}
