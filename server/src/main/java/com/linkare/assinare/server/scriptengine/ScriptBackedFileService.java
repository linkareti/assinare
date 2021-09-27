package com.linkare.assinare.server.scriptengine;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.linkare.assinare.sign.fileprovider.FileAccessException;
import com.linkare.assinare.sign.fileprovider.FileService;
import com.linkare.assinare.sign.model.AssinareDocument;

/**
 *
 * @author bnazare
 */
@Dependent
public class ScriptBackedFileService implements FileService {

    @Inject
    ScriptEngine scriptEngine;

    @Override
    public AssinareDocument getFile(String docName, Map<String, String> docParams) throws FileAccessException {
        return new ScriptBackedAssinareDocument(docName, docParams);
    }

    @Override
    public void putFile(String docName, AssinareDocument doc, Map<String, String> docParams) throws FileAccessException {
        try (final InputStream inputStream = doc.openInputStream()) {
            scriptEngine.storeDocument(docName, inputStream, docParams);
        } catch (IOException ex) {
            throw new FileAccessException(ex);
        }
    }

    private class ScriptBackedAssinareDocument implements AssinareDocument {

        private final String docName;
        private final Map<String, String> docParams;

        public ScriptBackedAssinareDocument(String docName, Map<String, String> docParams) {
            this.docName = docName;
            this.docParams = docParams;
        }

        @Override
        public String getName() {
            return docName;
        }

        @Override
        public String getContentType() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public InputStream openInputStream() throws IOException {
            return scriptEngine.loadDocument(docName, docParams);
        }

        @Override
        public OutputStream openOutputStream() throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

}
