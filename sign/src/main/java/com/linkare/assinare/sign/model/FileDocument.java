package com.linkare.assinare.sign.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.FileTypeMap;
import javax.activation.MimetypesFileTypeMap;

import org.apache.commons.io.IOUtils;

/**
 *
 * @author bnazare
 */
public class FileDocument implements AssinareDocument {
    
    private final File file;
    private InputStream exposedInputStream;
    private OutputStream exposedOutputStream;

    public FileDocument(String path) throws FileNotFoundException {
        this(new File(path));
    }
    
    public FileDocument(File file) throws FileNotFoundException {
        if (!file.exists()) {
            throw new FileNotFoundException("Ficheiro inexistente");
        }
        
        this.file = file;
    }
    
    public static FileDocument createTemporary() throws IOException {
        return createTemporary("ass", null);
    }
    
    public static FileDocument createTemporary(String prefix, String suffix) throws IOException {
        File tmpFile = File.createTempFile(prefix, suffix);
        tmpFile.deleteOnExit();
        
        return new FileDocument(tmpFile);
    }

    @Override
    public String getName() {
        return file.getName();
    }

    @Override
    public String getContentType() {
        FileTypeMap fileTypeMap = MimetypesFileTypeMap.getDefaultFileTypeMap();
        return fileTypeMap.getContentType(file);
    }

    @Override
    public InputStream openInputStream() throws IOException {
        closeExposedStreams();
        
        exposedInputStream = new FileInputStream(file);
        return exposedInputStream;
    }

    @Override
    public OutputStream openOutputStream() throws IOException {
        closeExposedStreams();
        
        exposedOutputStream = new FileOutputStream(file);
        return exposedOutputStream;
    }
    
    private void closeExposedStreams() {
        IOUtils.closeQuietly(exposedInputStream);
        exposedInputStream = null;
        
        IOUtils.closeQuietly(exposedOutputStream);
        exposedOutputStream = null;
    }
    
}
