package com.linkare.assinare.sign.model;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;

/**
 *
 * @author bnazare
 */
public class InMemoryDocument implements AssinareDocument {
    
    private final String name;
    private final String mimeType;
    private byte[] bytes;
    private ByteArrayOutputStream backingOutputStream;
    private InputStream exposedInputStream;
    private OutputStream exposedOutputStream;

    public InMemoryDocument(String name, byte[] bytes) {
        this(name, null, bytes);
    }
    
    public InMemoryDocument(String name, String mimeType, byte[] bytes) {
        this.name = name;
        this.mimeType = mimeType;
        this.bytes = bytes.clone();
    }
    
    public InMemoryDocument(String name, InputStream src) throws IOException {
        this(name, null, src);
    }
    
    public InMemoryDocument(String name, String mimeType, InputStream src) throws IOException {
        this.name = name;
        this.mimeType = mimeType;
        this.backingOutputStream = new ByteArrayOutputStream();
        backingOutputStream.write(src);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getContentType() {
        return mimeType;
    }

    @Override
    public InputStream openInputStream() {
        closeExposedStreams();
        
        if (backingOutputStream != null) {
            exposedInputStream = backingOutputStream.toInputStream();
        } else {
            exposedInputStream = new ByteArrayInputStream(bytes);
        }
        return exposedInputStream;
    }

    @Override
    public OutputStream openOutputStream() throws IOException {
        closeExposedStreams();
        
        if (backingOutputStream == null) {
            backingOutputStream = new ByteArrayOutputStream(bytes.length);
            backingOutputStream.write(bytes);
            bytes = null;
        } else {
            backingOutputStream.reset();
        }
        
        exposedOutputStream = backingOutputStream;
        return exposedOutputStream;
    }
    
    private void closeExposedStreams() {
        IOUtils.closeQuietly(exposedInputStream);
        exposedInputStream = null;
        
        IOUtils.closeQuietly(exposedOutputStream);
        exposedOutputStream = null;
    }
    
}
