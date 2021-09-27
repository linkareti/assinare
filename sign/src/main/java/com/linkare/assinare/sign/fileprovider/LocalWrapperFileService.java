package com.linkare.assinare.sign.fileprovider;

import java.util.Map;

import com.linkare.assinare.sign.model.AssinareDocument;

/**
 *
 * @author bnazare
 */
public class LocalWrapperFileService implements FileService {

    private final FileService delegateFileService;
    private final boolean delegateWrites;
    private final LocalFileService localFileService;

    public LocalWrapperFileService(FileService delegateFileService) {
        this(delegateFileService, false);
    }

    public LocalWrapperFileService(FileService delegateFileService, boolean delegateWrites) {
        this.delegateFileService = delegateFileService;
        this.delegateWrites = delegateWrites;
        this.localFileService = new LocalFileService();
    }

    @Override
    public AssinareDocument getFile(String docName, Map<String, String> docParams) throws FileAccessException {
        if (isLocalDocument(docName)) {
            return localFileService.getFile(docName, docParams);
        } else {
            return delegateFileService.getFile(docName, docParams);
        }
    }

    @Override
    public void putFile(String docName, AssinareDocument tmpFile, Map<String, String> docParams) throws FileAccessException {
        if (delegateWrites && isLocalDocument(docName)) {
            localFileService.putFile(docName, tmpFile, docParams);
        } else {
            delegateFileService.putFile(docName, tmpFile, docParams);
        }
    }

    private boolean isLocalDocument(final String docName) {
        return docName.startsWith("file://") || docName.startsWith("blob:");
    }
}
