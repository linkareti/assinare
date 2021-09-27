package com.linkare.assinare.sign.fileprovider;

import java.util.Map;

import com.linkare.assinare.sign.model.AssinareDocument;

/**
 *
 * @author bnazare
 */
public interface FileService {

    default AssinareDocument getFile(String docName) throws FileAccessException {
        return getFile(docName, null);
    }

    AssinareDocument getFile(String docName, Map<String, String> docParams) throws FileAccessException;

    default void putFile(String docName, AssinareDocument tmpFile) throws FileAccessException {
        putFile(docName, tmpFile, null);
    }

    void putFile(String docName, AssinareDocument tmpFile, Map<String, String> docParams) throws FileAccessException;

}
