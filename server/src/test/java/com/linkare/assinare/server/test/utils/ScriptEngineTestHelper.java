package com.linkare.assinare.server.test.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.Dependent;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.linkare.assinare.server.scriptengine.ScriptEngineConfiguration;

/**
 *
 * @author bnazare
 */
@Dependent
public class ScriptEngineTestHelper {

    private final File originalsDir;
    private final File signedDir;
    private final Set<File> createdFiles = new HashSet();

    public ScriptEngineTestHelper(ScriptEngineConfiguration conf) {
        File loadFile = new File(conf.loadFile());
        File storeFile = new File(conf.storeFile());

        if (loadFile.exists() && storeFile.exists()) {
            this.originalsDir = getDir(loadFile);
            this.signedDir = getDir(storeFile);
        } else {
            throw new IllegalArgumentException("Bad script engine configuration");
        }
    }

    private File getDir(File storeFile) {
        if (storeFile.isDirectory()) {
            return storeFile;
        } else {
            return storeFile.getParentFile();
        }
    }

    public File getOriginalFile(String fileName) {
        return new File(originalsDir, fileName);
    }

    public File getSignedFile(String fileName) {
        String signedFileName = getSignedFileName(fileName);
        return new File(signedDir, signedFileName);
    }

    public void putOriginalFile(String fileName, InputStream stream) throws IOException {
        putFile(originalsDir, fileName, stream);
    }

    public void putSignedFile(String fileName, InputStream stream) throws IOException {
        String signedFileName = getSignedFileName(fileName);
        putFile(signedDir, signedFileName, stream);
    }

    private String getSignedFileName(String docName) {
        return FilenameUtils.getBaseName(docName) + ".signed." + FilenameUtils.getExtension(docName);
    }

    private void putFile(File baseDir, String fileName, InputStream stream) throws IOException {
        File newFile = new File(baseDir, fileName);
        newFile.deleteOnExit();
        createdFiles.add(newFile);
        FileUtils.copyInputStreamToFile(stream, newFile);
    }

    public void deleteAll() {
        createdFiles.forEach(FileUtils::deleteQuietly);
        createdFiles.clear();
    }

}
