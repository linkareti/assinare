package com.linkare.assinare.daemon.net;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

import com.linkare.assinare.daemon.exception.HandlingException;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public abstract class AssinareBaseHandler implements HttpHandler {

    private static final String ASSINARE_DAEMON_NAME = "Assinare Daemon";

    public static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    public static final String HTTP_METHOD_GET = "GET";
    public static final String HTTP_METHOD_POST = "POST";
    public static final String HTTP_METHOD_PUT = "PUT";
    public static final String HTTP_METHOD_OPTIONS = "OPTIONS";

    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String ACCESS_CONTROL_ALLOW_ORIGIN_HEADER = "Access-Control-Allow-Origin";
    private static final String ACCESS_CONTROL_ALLOW_HEADERS_HEADER = "Access-Control-Allow-Headers";
    private static final String ACCESS_CONTROL_EXPOSE_HEADERS_HEADER = "Access-Control-Expose-Headers";
    private static final String SERVER_HEADER = "Server";
    private static final String CHARSET_HEADER = "charset";

    private final Logger log;
    private final String mainMethod;

    protected AssinareBaseHandler(String mainMethod) {
        this.log = Logger.getLogger(getClass().getName());
        this.mainMethod = mainMethod;
    }

    private void respondSuccessJson(HttpExchange t, Object jsonObject) throws IOException {
        String jsonString = jsonObject.toString();
        log.log(Level.INFO, "response json: {0}", jsonString);

        respond(t, HttpURLConnection.HTTP_OK, CONTENT_TYPE_APPLICATION_JSON, jsonString);
    }

    private void respondSuccessJson(HttpExchange t) throws IOException {
        respond(t, HttpURLConnection.HTTP_OK, CONTENT_TYPE_APPLICATION_JSON, "");
    }

    private void respondWithErrorJson(HttpExchange t, int responseCode, Object jsonObject) throws IOException {
        respond(t, responseCode, CONTENT_TYPE_APPLICATION_JSON, jsonObject.toString());
    }

    private void respondWithErrorJson(HttpExchange t, int responseCode) throws IOException {
        respond(t, responseCode, CONTENT_TYPE_APPLICATION_JSON, "");
    }

    private void respond(HttpExchange t, int responseCode, final String contentType, String response) throws IOException {
        // add the required response header
        Headers h = t.getResponseHeaders();
        h.add(CONTENT_TYPE_HEADER, contentType);
        h.add(ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "*");
        h.add(ACCESS_CONTROL_ALLOW_HEADERS_HEADER, "Origin, X-Requested-With, Content-Type, Accept");
        h.add(ACCESS_CONTROL_EXPOSE_HEADERS_HEADER, SERVER_HEADER);
        h.add(CHARSET_HEADER, DEFAULT_CHARSET.name());

        String implementationVersion = AssinareBaseHandler.class.getPackage().getImplementationVersion();
        if (implementationVersion != null) {
            h.add(SERVER_HEADER, ASSINARE_DAEMON_NAME + "/" + implementationVersion);
        } else {
            // this line should only be used when running without a JAR
            // e.g. in an IDE or with Maven exec plugin
            h.add(SERVER_HEADER, ASSINARE_DAEMON_NAME + "/2.7.0-SNAPSHOT");
        }

        byte[] reponseBytes = response.getBytes(DEFAULT_CHARSET);
        t.sendResponseHeaders(responseCode, reponseBytes.length);

        try (OutputStream os = t.getResponseBody()) {
            os.write(reponseBytes);
        }
    }

    protected String toJsonString(Map<String, ?> data) {
        return JSONObject.valueToString(data);
    }

    @Override
    public void handle(HttpExchange t) throws IOException {
        log.log(Level.INFO, "{0} request done to {1}", new Object[]{t.getRequestMethod(), getClass().getSimpleName()});

        if (t.getRequestMethod().contains(mainMethod)) {
            try {
                Object jsonObject = handleMainMethod(t);

                respondSuccessJson(t, jsonObject);

                afterSuccessResponseSent();
            } catch (HandlingException ex) {
                log.log(Level.SEVERE, ex.getMessage(), ex);
                respondWithErrorJson(t, HttpURLConnection.HTTP_INTERNAL_ERROR, ex.getJsonObject());
            }
        } else if (t.getRequestMethod().contains(HTTP_METHOD_OPTIONS)) {
            respondSuccessJson(t);
        } else {
            respondWithErrorJson(t, HttpURLConnection.HTTP_BAD_METHOD);
        }
    }

    protected abstract Object handleMainMethod(HttpExchange t) throws HandlingException;

    protected void afterSuccessResponseSent() {
        // NOOP
    }
}
