package com.linkare.assinare.sign.fileprovider;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FilenameUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.linkare.assinare.sign.model.AssinareDocument;
import com.linkare.assinare.sign.model.InMemoryDocument;

/**
 *
 * @author Bruno Nazaré - Linkare TI
 */
public class HTTPFileService implements FileService {

    private static final Logger LOG = Logger.getLogger(HTTPFileService.class.getName());

    public static final String COOKIE_HEADER_NAME = "Cookie";
    public static final String BINARY_PART_NAME = "bin";

    private static final boolean INCLUDE_SIGNED = false;

    private final HTTPFileServiceConfiguration config;

    public HTTPFileService(final HTTPFileServiceConfiguration config) {
        this.config = config;
    }

    @Override
    public AssinareDocument getFile(final String docName, Map<String, String> docParams) throws HTTPAssinareException {
        if (INCLUDE_SIGNED) {
            throw new UnsupportedOperationException("Not supported anymore.");
        } else {
            String docUrl;
            try {
                docUrl = config.getGetFileURLPrefix() + URLEncoder.encode(docName, "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                // this really shouldn't happen, so we just assume docUrl will always be defined
                docUrl = null;
                LOG.log(Level.SEVERE, null, ex);
            }
            HttpGet httpGet = new HttpGet(docUrl);
            httpGet.setHeader(COOKIE_HEADER_NAME, config.getCookieString());

            try (CloseableHttpClient client = HttpClients.createDefault();
                    CloseableHttpResponse response = client.execute(httpGet);) {
                if (response.getStatusLine().getStatusCode() == 404) {
                    throw new HTTPAssinareException(docName, docUrl, "Ficheiro inexistente", response);
                } else if (response.getStatusLine().getStatusCode() >= 400) {
                    throw new HTTPAssinareException(docName, docUrl, "Erro no pedido do ficheiro", response);
                } else {
                    return new InMemoryDocument(docName, response.getEntity().getContent());
                }
            } catch (IOException ioex) {
                throw new HTTPAssinareException(docName, docUrl, "Servidor não está acessível", ioex);
            }
        }
    }

    @Override
    public void putFile(String docName, AssinareDocument tmpFile, Map<String, String> docParams) throws HTTPAssinareException {
        final String putFileURL = config.getPutFileURL();
        HttpPost postMethod = new HttpPost(putFileURL);
        postMethod.setProtocolVersion(HttpVersion.HTTP_1_1);
        postMethod.setHeader(COOKIE_HEADER_NAME, config.getCookieString());

        try (InputStream docStream = tmpFile.openInputStream()) {
            postMethod.setEntity(createMultipartEntity(docStream, docName));

            try (CloseableHttpClient client = HttpClients.createDefault();
                    CloseableHttpResponse response = client.execute(postMethod);) {
                if (response.getStatusLine().getStatusCode() >= 400) {
                    throw new HTTPAssinareException(docName, putFileURL, "Erro HTTP no servidor remoto", response);
                }
            }
        } catch (IOException ioex) {
            throw new HTTPAssinareException(docName, putFileURL, "Erro ao salvar o ficheiro", ioex);
        }
    }

    private HttpEntity createMultipartEntity(final InputStream docStream, final String docName) throws IOException {
        String simpleFileName = FilenameUtils.getName(docName);

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.RFC6532);
        builder.addBinaryBody(BINARY_PART_NAME, docStream, ContentType.APPLICATION_OCTET_STREAM, simpleFileName);

        return builder.build();
    }
}
