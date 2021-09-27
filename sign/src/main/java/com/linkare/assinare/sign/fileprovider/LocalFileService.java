package com.linkare.assinare.sign.fileprovider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.linkare.assinare.sign.model.AssinareDocument;
import com.linkare.assinare.sign.model.FileDocument;

/**
 *
 * @author Paulo Zenida - Linkare TI
 *
 */
public class LocalFileService implements FileService {

    private static final String FILE_PROTOCOL = "file://";
    private static final String SIGNED_FILENAME_PART = ".signed.";

    @Override
    public AssinareDocument getFile(String docName, Map<String, String> docParams) throws FileAccessException {
        try {
            return new FileDocument(new File(docName.replace(FILE_PROTOCOL, "")));
        } catch (FileNotFoundException fnfex) {
            throw new FileAccessException("Erro ao abrir ficheiro local", fnfex);
        }
    }

    @Override
    public void putFile(String docName, AssinareDocument tmpFile, Map<String, String> docParams) throws FileAccessException {
        // This code is here to help with debug
        // It should not be used by the application itself
        String targetFileName = FilenameUtils.removeExtension(docName) + SIGNED_FILENAME_PART + FilenameUtils.getExtension(docName);
        File targetFile = new File(targetFileName.replace(FILE_PROTOCOL, ""));

        try (InputStream tmpInputStream = tmpFile.openInputStream()) {
            FileUtils.copyToFile(tmpInputStream, targetFile);
        } catch (IOException ex) {
            throw new FileAccessException(ex);
        }
    }
}
