package com.linkare.assinare.server.test.mocks;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import com.linkare.assinare.server.scriptengine.ScriptFileNotFoundException;
import com.linkare.assinare.sign.fileprovider.FileAccessException;
import com.linkare.assinare.sign.fileprovider.FileService;
import com.linkare.assinare.sign.model.AssinareDocument;
import com.linkare.assinare.sign.model.InMemoryDocument;

/**
 *
 * @author bnazare
 */
public class MockFileService implements FileService {

    private static final Map<String, byte[]> FILES = new HashMap<>();

    @Override
    public AssinareDocument getFile(String docName, Map<String, String> docParams) throws FileAccessException {
        if (FILES.containsKey(docName)) {
            return new InMemoryDocument(docName, FILES.get(docName));
        } else {
            throw new FileAccessException(new IOException(new ScriptFileNotFoundException("mock file not found")));
        }
    }

    @Override
    public void putFile(String docName, AssinareDocument doc, Map<String, String> docParams) throws FileAccessException {
        final String signedDocName = getSignedDocName(docName);
        try (final InputStream is = doc.openInputStream()) {
            final byte[] docData = IOUtils.toByteArray(is);
            FILES.put(signedDocName, docData);
        } catch (IOException ex) {
            throw new FileAccessException(ex);
        }
    }

    private String getSignedDocName(String docName) {
        return FilenameUtils.getBaseName(docName) + ".signed." + FilenameUtils.getExtension(docName);
    }

    public void clearFiles() {
        FILES.clear();
    }

    public byte[] getSignedFile(String docName) {
        final String signedDocName = getSignedDocName(docName);
        return FILES.get(signedDocName);
    }

    public void putUnsignedFile(String docName, byte[] docData) {
        FILES.put(docName, docData);
    }

}
