package com.linkare.assinare.daemon.handler.signature;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.linkare.assinare.commons.AssinareError;
import com.linkare.assinare.commons.AssinareException;
import com.linkare.assinare.daemon.exception.HandlingException;
import com.linkare.assinare.daemon.net.AssinareBaseHandler;
import com.linkare.assinare.sign.AssinareConstants;
import com.linkare.assinare.sign.AssinareSign;
import com.linkare.assinare.sign.fileprovider.HTTPAssinareException;
import com.linkare.assinare.sign.fileprovider.HTTPFileServiceConfiguration;
import com.linkare.assinare.sign.pdf.itext.TSAAssinareException;
import com.sun.net.httpserver.HttpExchange;

public abstract class AssinareBaseSignatureHandler extends AssinareBaseHandler {

    private static final Logger LOG = Logger.getLogger(AssinareBaseSignatureHandler.class.getName());

    protected final AssinareSign assinareMain = new AssinareSign();

    protected AssinareBaseSignatureHandler() {
        super(HTTP_METHOD_POST);
    }

    @Override
    public final Object handleMainMethod(final HttpExchange t) throws HandlingException {
        final String reqBody;
        try {
            reqBody = IOUtils.toString(t.getRequestBody(), StandardCharsets.UTF_8);
            LOG.log(Level.INFO, "Request Body: {0}", reqBody);
        } catch (IOException ex) {
            String signatureStatus = AssinareConstants.SIGNATURE_BATCH_ERROR_CODE;
            String msg = "Ocorreu um erro interno.";

            JSONObject errorObject = buildResponseObject(null, signatureStatus, msg);
            throw new HandlingException(ex, errorObject);
        }

        final SignatureParameters params = new SignatureParameters(reqBody);

        final String[] docList = params.getDocs();
        try {
            assinareMain.reconfigure(calcHttpConfiguration(params));

            String result = handleSignature(params);
            String signatureStatus = AssinareConstants.BATCH_END_CODE;
            String msg = "Final do processo de assinatura: " + result;

            return buildResponseObject(Arrays.deepToString(docList), signatureStatus, msg);
        } catch (RuntimeException rex) {
            String signatureStatus = AssinareConstants.SIGNATURE_BATCH_ERROR_CODE;
            String msg = "Ocorreu um erro interno.";

            Throwable cause = rex.getCause();
            if (cause instanceof AssinareException) {
                msg = cause.getMessage();
            }

            JSONObject errorObject = buildResponseObject(Arrays.deepToString(docList), signatureStatus, msg);
            throw new HandlingException(rex, errorObject);
        } catch (HTTPAssinareException haex) {
            String signatureStatus = AssinareConstants.SIGNATURE_DOC_GET_HTTP_ERROR_CODE;
            String msg = haex.getMessage();
            final JSONObject leafObj = buildHttpResponseJSObject(haex);

            JSONObject errorObject = buildResponseObject(haex.getDocName(), signatureStatus, msg, "http", leafObj);
            throw new HandlingException(haex, errorObject);
            // FIXME: we can not answer a REST request twice
//            JSONObject batchErrorObject = getResponseObject(Arrays.deepToString(docList), AssinareConstants.SIGNATURE_BATCH_ERROR_CODE, msg); 
//            throw new HandlingException(haex, batchErrorObject);
        } catch (TSAAssinareException taex) {
            String signatureStatus = AssinareConstants.SIGNATURE_DOC_TSA_ERROR_CODE;
            String msg = taex.getMessage();

            JSONObject errorObject = buildResponseObject(Arrays.deepToString(docList), signatureStatus, msg);
            throw new HandlingException(taex, errorObject);
        } catch (AssinareException | AssinareError aex) {
            String signatureStatus = AssinareConstants.SIGNATURE_BATCH_ERROR_CODE;
            String msg = aex.getMessage();

            JSONObject errorObject = buildResponseObject(Arrays.deepToString(docList), signatureStatus, msg);
            throw new HandlingException(aex, errorObject);
        }
    }

    protected abstract String handleSignature(SignatureParameters params) throws AssinareException, AssinareError;

    protected HTTPFileServiceConfiguration calcHttpConfiguration(final SignatureParameters params) {
        final String getFileURLPrefix = params.getGetFileUrlPrefix();
        final String getSignedFileURLPrefix = params.getGetSignedFileUrlPrefix();
        final String putFileURL = params.getPutFileUrl();
        final String cookieStr = params.getAuthCookies();

        // Log parameters
        LOG.log(Level.INFO, "getFileURLPrefix: {0}", getFileURLPrefix);
        LOG.log(Level.INFO, "getSignedFileURLPrefix: {0}", getSignedFileURLPrefix);
        LOG.log(Level.INFO, "putFileURL: {0}", putFileURL);
        LOG.log(Level.INFO, "cookieStr: {0}", cookieStr);

        return new HTTPFileServiceConfiguration(getFileURLPrefix, getSignedFileURLPrefix, putFileURL, cookieStr);
    }

    private JSONObject buildResponseObject(final String docName, final String signatureStatus, final String msg, final String leafJSObjName, final JSONObject leafJSObj) {
        JSONObject newObj = buildResponseObject(docName, signatureStatus, msg);
        if (leafJSObjName != null && leafJSObj != null) {
            newObj.put(leafJSObjName, leafJSObj);
        }

        return newObj;
    }

    private JSONObject buildResponseObject(final String docName, final String signatureStatus, final String msg) throws JSONException {
        final JSONObject newObj = new JSONObject();
        newObj.put("docName", docName);
        newObj.put("signatureStatus", signatureStatus);
        newObj.put("message", msg);

        return newObj;
    }

    private JSONObject buildHttpResponseJSObject(final HTTPAssinareException haex) {
        final JSONObject rootObj = new JSONObject();
        final JSONObject headersObj = buildHeadersJSObject(haex.getHeaderFields());
        rootObj.put("headers", headersObj);
        rootObj.put("status", haex.getStatusCode());
        rootObj.put("message", haex.getResponseMessage());
        return rootObj;
    }

    private JSONObject buildHeadersJSObject(final Map<String, List<String>> headers) {
        final JSONObject headersObj = new JSONObject();

        if (headers != null) {
            headers.entrySet().forEach(entrySet -> {
                String key = entrySet.getKey();
                final String value = StringUtils.join(entrySet.getValue(), ",");
                if (key == null) {
                    key = "null";
                }
                headersObj.put(key, value);
            });
        }

        return headersObj;
    }
}
