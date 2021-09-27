package com.linkare.assinare.daemon.handler.signature;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import com.linkare.assinare.sign.SignatureRenderingMode;
import com.linkare.assinare.sign.asic.ASiCSignatureFields;
import com.linkare.assinare.sign.asic.CommitmentType;
import com.linkare.assinare.sign.pdf.PDFSignatureFields;
import com.linkare.assinare.sign.pdf.UiMode;

public class SignatureParameters {

    private static final String SIGNATURE_PARAMS_PROP_NAME = "signatureParams";

    private final JSONObject params;
    private final String[] docs;

    public SignatureParameters(final String jsonParameters) {
        this.params = new JSONObject(jsonParameters);
        this.docs = this.loadDocs();
    }

    private String[] loadDocs() {
        final JSONArray jsonArray = params.getJSONArray("docs");
        final List<String> resultList = new LinkedList<>();
        for (Object obj : jsonArray) {
            resultList.add((String) obj);
        }
        return resultList.toArray(new String[resultList.size()]);
    }

    public String[] getDocs() {
        return this.docs.clone();
    }

    public String getGetFileUrlPrefix() {
        return this.params.getString("getFileUrlPrefix");
    }

    public String getGetSignedFileUrlPrefix() {
        return this.params.optString("getSignedFileUrlPrefix", null);
    }

    public String getPutFileUrl() {
        return this.params.getString("putFileUrl");
    }

    public String getAuthCookies() {
        return this.params.getString("authCookies");
    }

    public PDFSignatureFields getSignatureFields() {
        if (params.has(SIGNATURE_PARAMS_PROP_NAME)) {
            JSONObject sigParams = params.getJSONObject(SIGNATURE_PARAMS_PROP_NAME);

            String contact = sigParams.optString("contact", null);
            String location = sigParams.optString("location", null);
            String reason = sigParams.optString("reason", null);

            Double percentX = null;
            if (sigParams.has("percentX")) {
                percentX = sigParams.getDouble("percentX");
            }

            Double percentY = null;
            if (sigParams.has("percentY")) {
                percentY = sigParams.getDouble("percentY");
            }

            Integer pageNumber = null;
            if (sigParams.has("pageNumber")) {
                pageNumber = sigParams.getInt("pageNumber");
            }

            Integer width = null;
            if (sigParams.has("width")) {
                width = sigParams.getInt("width");
            }

            Integer height = null;
            if (sigParams.has("height")) {
                height = sigParams.getInt("height");
            }

            SignatureRenderingMode sigRenderingMode = null;
            if (sigParams.has("sigRenderingMode")) {
                sigRenderingMode = SignatureRenderingMode.valueOf(sigParams.getString("sigRenderingMode"));
            }

            String fieldName = sigParams.optString("fieldName", null);

            String tsaUrl = sigParams.optString("tsaUrl", null);

            Boolean doLTV = null;
            if (sigParams.has("doLTV")) {
                doLTV = sigParams.getBoolean("doLTV");
            }

            URL logoFileURL = null;
            if (sigParams.has("logoFileURL")) {
                try {
                    logoFileURL = new URL(sigParams.optString("logoFileURL", null));
                } catch (MalformedURLException ex) {
                    Logger.getLogger(SignatureParameters.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            UiMode uiMode = null;
            if (sigParams.has("uiMode")) {
                uiMode = UiMode.valueOf(sigParams.getString("uiMode"));
            }

            return new PDFSignatureFields(contact, location, reason,
                    percentX, percentY, pageNumber, width, height,
                    sigRenderingMode, fieldName, tsaUrl, doLTV, logoFileURL,
                    uiMode);
        } else {
            return new PDFSignatureFields();
        }
    }

    public ASiCSignatureFields getASiCSignatureFields() {
        if (params.has(SIGNATURE_PARAMS_PROP_NAME)) {
            JSONObject sigParams = params.getJSONObject(SIGNATURE_PARAMS_PROP_NAME);

            String containerName = sigParams.optString("containerName", null);
            String location = sigParams.optString("location", null);
            String claimedRole = sigParams.optString("claimedRole", null);

            CommitmentType commitmentType = null;
            if (sigParams.has("commitmentType")) {
                commitmentType = CommitmentType.valueOf(sigParams.getString("commitmentType"));
            }

            String tsaUrl = sigParams.optString("tsaUrl", null);

            Boolean withLTA = null;
            if (sigParams.has("withLTA")) {
                withLTA = sigParams.getBoolean("withLTA");
            }

            return new ASiCSignatureFields(containerName, location, claimedRole, commitmentType, tsaUrl, withLTA);
        } else {
            return new ASiCSignatureFields();
        }
    }
}
