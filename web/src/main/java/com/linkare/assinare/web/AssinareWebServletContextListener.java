package com.linkare.assinare.web;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.apache.commons.io.FileUtils;

/**
 *
 * @author rvaz
 */
@WebListener
public class AssinareWebServletContextListener implements ServletContextListener {

    private static final Logger LOGGER = Logger.getLogger(AssinareWebServletContextListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LOGGER.log(Level.FINE, ">>>>AssinareWebServletContextListener.contextInitialized>>>>");
        Config.initParams(sce.getServletContext());

        deleteSignedFilesDirectory();
        deleteStoredFilesDirectory();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOGGER.log(Level.FINE, ">>>>AssinareWebServletContextListener.contextDestroyed>>>>");
        deleteSignedFilesDirectory();
        deleteStoredFilesDirectory();
    }

    private void deleteSignedFilesDirectory() {
        cleanDir(Config.getSignedFilesLocation());
    }

    private void deleteStoredFilesDirectory() {
        cleanDir(Config.getStoreFilesLocation());
    }

    private void cleanDir(String fileNameDir) {
        LOGGER.log(Level.FINE, "Cleaning directory >>> {0}", fileNameDir);
        File signedFilesDir = new File(fileNameDir);
        try {
            FileUtils.cleanDirectory(signedFilesDir);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        LOGGER.log(Level.FINE, "Cleaning directory completed >>>>>{0}", signedFilesDir.getAbsolutePath());
    }
}
