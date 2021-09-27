package com.linkare.assinare.sign.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author bnazare
 */
public interface AssinareDocument {
    
    String getName();
    
    String getContentType();
    
    InputStream openInputStream() throws IOException;
    
    OutputStream openOutputStream() throws IOException;
    
}
