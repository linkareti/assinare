package com.linkare.assinare.applet;

import static com.linkare.assinare.applet.common.utils.AppletUtils.validatePreConditions;

import java.applet.Applet;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JApplet;

import org.apache.commons.lang3.StringUtils;

import com.linkare.assinare.applet.common.async.AsyncPrivilegedAction;
import com.linkare.assinare.applet.common.utils.JSObjectUtils;
import com.linkare.assinare.commons.AssinareError;
import com.linkare.assinare.commons.AssinareException;
import com.linkare.assinare.sign.AssinareConstants;
import com.linkare.assinare.sign.AssinareSign;
import com.linkare.assinare.sign.SignatureRenderingMode;
import com.linkare.assinare.sign.asic.ASiCSignatureFields;
import com.linkare.assinare.sign.asic.CommitmentType;
import com.linkare.assinare.sign.fileprovider.HTTPAssinareException;
import com.linkare.assinare.sign.fileprovider.HTTPFileServiceConfiguration;
import com.linkare.assinare.sign.pdf.PDFSignatureFields;
import com.linkare.assinare.sign.pdf.UiMode;
import com.linkare.assinare.sign.pdf.itext.TSAAssinareException;

import netscape.javascript.JSObject;

/**
 *
 * @author bnazare
 */
public class AssinareApplet extends JApplet {

    private static final long serialVersionUID = 1L;
    private static final String COUNTRY_PARAM = "country";
    private static final String LANGUAGE_PARAM = "language";
    private static final String DEFAULT_CALLBACK_NAME = "signingDoneMsg";
    private static final Logger LOG = Logger.getLogger(AssinareApplet.class.getName());

    private AssinareSign main;
    private Object sigSwingWorker;
    // FIXME: old code below
//    private AbstractInitalizeSignatureSwingWorker sigSwingWorker;

    private ExecutorService executor;
    private boolean busy = false;

    /**
     * Initialization method that will be called after the applet is loaded into
     * the browser.
     */
    @Override
    public void init() {
        // TODO  remove the following separate_jvm param existence check since it 
        // is no longer needed do not forget to remove it also in the definition of the 
        // applet's params in the webpage
        if (getParameter("separate_jvm") == null || !getParameter("separate_jvm").equalsIgnoreCase("true")) {
            throw new IllegalArgumentException("Applet falhou a inicialização: parâmetro separate_jvm em falta ou não é 'true'");
        }

        try {
            main = new AssinareSign(buildHttpFileServiceConfig(this));
        } catch (AssinareException ex) {
            LOG.log(Level.SEVERE, null, ex);
            throw new IllegalArgumentException("Applet falhou a inicialização: ", ex);
        }

        executor = Executors.newSingleThreadExecutor();

//	this.callJs("appletStartCallback", new Object[0]);
        this.loadDefaultLocaleFromParameters();
        LOG.info("Assinare Sign Applet iniciada.");
    }

    private static HTTPFileServiceConfiguration buildHttpFileServiceConfig(Applet applet) throws AssinareException {
        final String getFileURLPrefix = getAppletParameter(applet, "get_file_url_prefix");
        final String getSignedFileURLPrefix = getAppletParameter(applet, "get_signed_file_url_prefix", false);
        final String putFileURL = getAppletParameter(applet, "put_file_url");
        final String cookieString = getAppletParameter(applet, "auth_cookies");

        return new HTTPFileServiceConfiguration(getFileURLPrefix, getSignedFileURLPrefix, putFileURL, cookieString);
    }

    private static String getAppletParameter(Applet applet, String paramName) throws AssinareException {
        return getAppletParameter(applet, paramName, true);
    }

    private static String getAppletParameter(Applet applet, String paramName, boolean showException) throws AssinareException {
        final String parameter = applet.getParameter(paramName);
        if (showException && parameter == null) {
            throw new AssinareException(paramName + " is missing.");
        } else {
            return parameter;
        }
    }

    /**
     * Method called from JavaScript used to initiate the PAdES signing process.
     * <p>
     * This method is to be called from JavaScript in the host web page where
     * the Assinare Applet is initiated. The results of the signing process are
     * sent
     *
     * @param docNames filenames of the documents chosen to be signed from
     * separated by comma
     * @param callbackName
     * @param jsSigFields
     * @throws AssinareException
     * @throws AssinareError
     * @see PdfSignatureSwingWorker
     */
    public void signDocuments(final Object docNames, final String callbackName, final JSObject jsSigFields) throws AssinareException, AssinareError {
        final JSObject window = JSObject.getWindow(this);
        this.checkIfWorkerIsBusy();
        busy = true;
        validatePreConditions(window, callbackName);

        String[] docList = parseDocNames(docNames);
        final PDFSignatureFields sigFields = parseSigFields(jsSigFields);

        AsyncPrivilegedAction<String> asyncAction = new DataResponseAction(docList, callbackName) {
            @Override
            protected String doSigning() throws AssinareError, AssinareException {
                return main.signPdf(docList, sigFields);
            }
        };

        executor.submit(asyncAction);
    }

    public void signDocuments(final Object docNames, final String callbackName) throws AssinareException, AssinareError {
        signDocuments(docNames, callbackName, null);
    }

    @Deprecated
    public void signDocuments(final Object docNames) throws AssinareException, AssinareError {
        signDocuments(docNames, DEFAULT_CALLBACK_NAME, null);
    }

    /**
     * Method called from JavaScript used to initiate the XAdES signing process
     * with the inclusion of the signed files inside an ASiC container.
     * <p>
     * This method is to be called from JavaScript in the host web page where
     * the Assinare Applet is initiated.
     *
     *
     * @param docNames filenames of the documents chosen to be signed from
     * separated by comma
     * @param callbackName
     * @param jsSigFields
     * @throws AssinareException
     * @throws AssinareError
     * @see ContainerSignatureSwingWorker
     *
     */
    public void signContainer(final Object docNames, final String callbackName, final JSObject jsSigFields) throws AssinareException, AssinareError {
        final JSObject window = JSObject.getWindow(this);
        this.checkIfWorkerIsBusy();
        busy = true;
        validatePreConditions(window, callbackName);

        final String[] docList = parseDocNames(docNames);
        final ASiCSignatureFields sigFields = parseASiCSigFields(jsSigFields);

        AsyncPrivilegedAction<String> asyncAction = new DataResponseAction(docList, callbackName) {
            @Override
            protected String doSigning() throws AssinareError, AssinareException {
                return main.signContainer(docList, sigFields);
            }
        };

        executor.submit(asyncAction);
    }

    @Deprecated
    public void signContainer(final Object docNames, final String callbackName) throws AssinareException, AssinareError {
        signContainer(docNames, callbackName, null);
    }

    @Deprecated
    public void signContainer(final Object docNames) throws AssinareException, AssinareError {
        signContainer(docNames, DEFAULT_CALLBACK_NAME, null);
    }

    /**
     * Method called from JavaScript that uploads files and makes those
     * available to be chosen and signed called from the remote host .
     *
     * Files are included creating a jsArray and then invoking push JavaScript
     * function to include the uploaded files.
     *
     * @return fileNames in form of a JSObject/jsArray
     * @throws Exception
     */
    public JSObject chooseLocalFiles() throws Exception {
        try {
            String[] fNames = AccessController.doPrivileged(
                    (PrivilegedExceptionAction<String[]>) () -> main.chooseLocalFiles()
            );

            final JSObject jsWindow = JSObject.getWindow(this);
            return JSObjectUtils.createJSArray(jsWindow, (Object[]) fNames);
        } catch (final PrivilegedActionException paex) {
            LOG.log(Level.FINEST, null, paex);
            LOG.log(Level.SEVERE, null, paex.getCause());
            throw paex.getException();
        }
    }

    private void checkIfWorkerIsBusy() throws AssinareException {
        if (sigSwingWorker != null && busy) {
            AssinareException asex = new AssinareException("Processo de assinatura em curso.");
            this.callJs("writeMessage", new Object[]{asex.toString()});
            throw asex;
        }
    }

    private void callJs(final String methodName, final Object... args) {
        final JSObject window = JSObject.getWindow(this);
        window.call(methodName, args);// FIXME writeMessage function NON-EXISTENT?
    }

    private void loadDefaultLocaleFromParameters() {
        final String language = getParameter(LANGUAGE_PARAM);
        final String country = getParameter(COUNTRY_PARAM);

        if (language != null) {
            if (country != null) {
                Locale.setDefault(new Locale(language, country));
            } else {
                Locale.setDefault(new Locale(language));
            }
        }

        LOG.log(Level.INFO, "Applet''s Locale default''s display name: {0}", Locale.getDefault().toLanguageTag());
    }

    private String[] parseDocNames(final Object docNames) {
        String[] docList = new String[0];

        if (docNames instanceof JSObject) {
            JSObject docNamesJso = (JSObject) docNames;
            docList = JSObjectUtils.getJSArray(docNamesJso, String.class);
        } else if (docNames instanceof String) {
            String docNamesStr = (String) docNames;
            if (StringUtils.isBlank(docNamesStr)) {
                docList = docNamesStr.split(",", -1);
            }
        }

        return docList;
    }

    private PDFSignatureFields parseSigFields(final JSObject jsObject) {
        if (jsObject != null) {
            String contact = (String) JSObjectUtils.safeGetMember(jsObject, "contact");
            String location = (String) JSObjectUtils.safeGetMember(jsObject, "location");
            String reason = (String) JSObjectUtils.safeGetMember(jsObject, "reason");
            Double percentX = (Double) JSObjectUtils.safeGetMember(jsObject, "percentX");
            Double percentY = (Double) JSObjectUtils.safeGetMember(jsObject, "percentY");
            Integer pageNumber = (Integer) JSObjectUtils.safeGetMember(jsObject, "pageNumber");
            Integer width = (Integer) JSObjectUtils.safeGetMember(jsObject, "width");
            Integer height = (Integer) JSObjectUtils.safeGetMember(jsObject, "height");

            SignatureRenderingMode sigRenderingMode = null;
            if (JSObjectUtils.safeGetMember(jsObject, "sigRenderingMode") != null) {
                sigRenderingMode = SignatureRenderingMode.valueOf((String) JSObjectUtils.safeGetMember(jsObject, "sigRenderingMode"));
            }

            String fieldName = (String) JSObjectUtils.safeGetMember(jsObject, "fieldName");
            String tsaUrl = (String) JSObjectUtils.safeGetMember(jsObject, "tsaUrl");
            Boolean doLTV = (Boolean) JSObjectUtils.safeGetMember(jsObject, "doLTV");
            URL logoFileURL = null;
            if (JSObjectUtils.safeGetMember(jsObject, "logoFileURL") != null) {
                try {
                    logoFileURL = new URL((String) JSObjectUtils.safeGetMember(jsObject, "logoFileURL"));
                } catch (MalformedURLException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
            }

            UiMode uiMode = null;
            if (JSObjectUtils.safeGetMember(jsObject, "uiMode") != null) {
                uiMode = UiMode.valueOf((String) JSObjectUtils.safeGetMember(jsObject, "uiMode"));
            }

            return new PDFSignatureFields(contact, location, reason,
                    percentX, percentY, pageNumber, width, height,
                    sigRenderingMode, fieldName, tsaUrl, doLTV, logoFileURL,
                    uiMode);
        } else {
            return new PDFSignatureFields();
        }
    }

    private ASiCSignatureFields parseASiCSigFields(final JSObject jsObject) {
        if (jsObject != null) {
            String containerName = (String) JSObjectUtils.safeGetMember(jsObject, "containerName");
            String location = (String) JSObjectUtils.safeGetMember(jsObject, "location");
            String claimedRole = (String) JSObjectUtils.safeGetMember(jsObject, "claimedRole");
            String tsaUrl = (String) JSObjectUtils.safeGetMember(jsObject, "tsaUrl");
            Boolean withLTA = (Boolean) JSObjectUtils.safeGetMember(jsObject, "withLTA");

            CommitmentType commitmentType = null;
            if (JSObjectUtils.safeGetMember(jsObject, "commitmentType") != null) {
                commitmentType = CommitmentType.valueOf((String) JSObjectUtils.safeGetMember(jsObject, "commitmentType"));
            }

            return new ASiCSignatureFields(containerName, location, claimedRole, commitmentType, tsaUrl, withLTA);
        } else {
            return new ASiCSignatureFields();
        }
    }

    private abstract class DataResponseAction extends AsyncPrivilegedAction<String> {

        private final String[] docList;
        private final String callbackName;

        public DataResponseAction(String[] docList, String callbackName) {
            this.docList = docList;
            this.callbackName = callbackName;
        }

        @Override
        protected String runPrivileged() {
            try {
                String result = doSigning();
                String signatureStatus = AssinareConstants.BATCH_END_CODE;
                String msg = "Final do processo de assinatura: " + result;

                signingDoneSuccess(callbackName, Arrays.deepToString(docList), signatureStatus, msg);
                return result;
            } catch (RuntimeException rex) {
                LOG.log(Level.SEVERE, rex.getMessage(), rex);
                String signatureStatus = AssinareConstants.SIGNATURE_BATCH_ERROR_CODE;
                String msg = "Ocorreu um erro interno.";

                Throwable cause = rex.getCause();
                if (cause instanceof AssinareException) {
                    msg = cause.getMessage();
                }

                signingDoneError(callbackName, Arrays.deepToString(docList), signatureStatus, msg);
                return null;
            } catch (HTTPAssinareException haex) {
                LOG.log(Level.SEVERE, null, haex);
                String signatureStatus = AssinareConstants.SIGNATURE_DOC_GET_HTTP_ERROR_CODE;
                String msg = haex.getMessage();
                final Map<String, Object> leafObj = buildHttpResponseJSObject(haex);

                signingDoneError(callbackName, haex.getDocName(), signatureStatus, msg, "http", leafObj);
                signingDoneError(callbackName, Arrays.deepToString(docList), AssinareConstants.SIGNATURE_BATCH_ERROR_CODE, msg);
                return null;
            } catch (TSAAssinareException taex) {
                LOG.log(Level.SEVERE, taex.getMessage(), taex);
                String signatureStatus = AssinareConstants.SIGNATURE_DOC_TSA_ERROR_CODE;
                String msg = taex.getMessage();

                signingDoneError(callbackName, Arrays.deepToString(docList), signatureStatus, msg);
                return null;
            } catch (AssinareException | AssinareError aex) {
                LOG.log(Level.SEVERE, aex.getMessage(), aex);
                String signatureStatus = AssinareConstants.SIGNATURE_BATCH_ERROR_CODE;
                String msg = aex.getMessage();

                signingDoneError(callbackName, Arrays.deepToString(docList), signatureStatus, msg);
                return null;
            } finally {
                busy = false;
            }
        }

        protected abstract String doSigning() throws AssinareError, AssinareException;

        /**
         * Method sends information on the signing process calling the
         * signingDoneMsg JavaScript function to send the information regarding
         * the signing process of a file.
         *
         * The signingDoneMsg function is called from this method passing as a
         * parameter a map structure containing the docName, signatureStatus,
         * msg strings and also other leafJSObjName, leafJSObj that can contain
         * error code and information regarding the get of chosen files (@see
         * AbstractFinalizeSignatureSwingWorker.getRemoteFile) and the put of
         * the signed files (@see AbstractFinalizeSignatureSwingWorker.putFile)
         *
         * @param docName document's filename
         * @param signatureStatus status of the signing process of
         * {@link docName}
         * @param msg more detailed information about the signing process of
         * {@link docName}
         * @param leafJSObjName key value for the {@link leafJSObj}
         * @param leafJSObj value or key/value structure with more detailed
         * information
         *
         * @see
         * com.linkare.assinare.applet.AbstractFinalizeSignatureSwingWorker#printMessage(String,
         * String, String, String, JSObject)
         * @see com.linkare.assinare.applet.AbstractFinalizeSignatureSwingWorker
         */
        private void signingDoneError(final String callbackName, final String docName, final String signatureStatus, final String msg, final String leafJSObjName, final Map<String, Object> leafJSObj) {
            Map<String, Object> map = buildMsgMap(docName, signatureStatus, msg);
            if (leafJSObjName != null && leafJSObj != null) {
                map.put(leafJSObjName, leafJSObj);
            }

            final JSObject jsWindow = JSObject.getWindow(AssinareApplet.this);
            final JSObject jsObject = JSObjectUtils.createJSObject(jsWindow, map);

            jsWindow.call(callbackName, jsObject, null);
        }

        private void signingDoneError(final String callbackName, final String docName, final String signatureStatus, final String msg) {
            signingDoneError(callbackName, docName, signatureStatus, msg, null, null);
        }

        private void signingDoneSuccess(final String callbackName, final String docName, final String signatureStatus, final String msg) {
            Map<String, Object> map = buildMsgMap(docName, signatureStatus, msg);

            final JSObject jsWindow = JSObject.getWindow(AssinareApplet.this);
            final JSObject jsObject = JSObjectUtils.createJSObject(jsWindow, map);

            jsWindow.call(callbackName, null, jsObject);
        }

        private Map<String, Object> buildMsgMap(final String docName, final String signatureStatus, final String msg) {
            final Map<String, Object> map = new HashMap<>();
            map.put("docName", docName);
            map.put("signatureStatus", signatureStatus);
            map.put("message", msg);

            return map;
        }

        private Map<String, Object> buildHttpResponseJSObject(final HTTPAssinareException haex) {
            final Map<String, Object> rootObj = new HashMap<>();
            final Map<String, Object> headersObj = buildHeadersJSObject(haex.getHeaderFields());
            rootObj.put("headers", headersObj);
            rootObj.put("status", haex.getStatusCode());
            rootObj.put("message", haex.getResponseMessage());
            return rootObj;
        }

        private Map<String, Object> buildHeadersJSObject(final Map<String, List<String>> headers) {
            final Map<String, Object> headersObj = new HashMap<>();

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
}
