package com.linkare.assinare.web;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.commons.io.FileUtils;

/**
 *
 * @author Ricardo Vaz - Linkare TI
 * @author Paulo Zenida - Linkare TI
 *
 */
@WebListener
public class AssinareWebHTTPSessionListener implements HttpSessionListener {

    private static final Logger LOGGER = Logger.getLogger(AssinareWebHTTPSessionListener.class.getName());

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        // do nothing - the signature process creates the folders automatically, as needed
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        LOGGER.log(Level.FINE, ">>>>AssinareWebServletSessionListener.sessionDestroyed>>>>{0}", se.getSession().getId());
        deleteSessionDirectory(se);
    }

    private void deleteSessionDirectory(final HttpSessionEvent se) {
        String fileNameDir = Config.getSignedFilesLocation() + "/" + se.getSession().getId();
        LOGGER.log(Level.FINE, "fileNameDir>>>{0}", fileNameDir);

        File file = new File(fileNameDir);
        try {
            FileUtils.deleteDirectory(file);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        LOGGER.log(Level.FINE, ">>>>>{0}", file.getAbsolutePath());
    }
}
