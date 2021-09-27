package com.linkare.assinare.web;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

/**
 *
 * @author bnazare
 * @author ralves
 */
public final class Config {

    private static final Logger LOGGER = Logger.getLogger(Config.class.getName());

    private static final String SIGNED_FILES_LOCATION_PARAM = "signed.files.location";
    private static final String ORIGINAL_FILES_LOCATION_PARAM = "original.files.location";
    private static final String STORE_FILES_LOCATION_PARAM = "store.files.location";

    private static final String ORIGINAL_FILES_DEFAULT_PATH = "assinare/original";
    private static final String SIGNED_FILES_DEFAULT_PATH = "assinare/signed";
    private static final String STORE_FILES_DEFAULT_PATH = "assinare/files";

    private static String originalFilesLocation;
    private static String signedFilesLocation;
    private static String storeFilesLocation;

    public static String getOriginalFilesLocation() {
        return originalFilesLocation;
    }

    public static String getSignedFilesLocation() {
        return signedFilesLocation;
    }

    public static String getStoreFilesLocation() {
        return storeFilesLocation;
    }

    static void initParams(final ServletContext ctx) {
        final String relativeFilesLocation = ctx.getInitParameter(ORIGINAL_FILES_LOCATION_PARAM);
        final String relativeSignedFilesLocation = ctx.getInitParameter(SIGNED_FILES_LOCATION_PARAM);
        final String relativeStoreFilesLocation = ctx.getInitParameter(STORE_FILES_LOCATION_PARAM);
        originalFilesLocation = makeAbsoluteToHome(relativeFilesLocation, ORIGINAL_FILES_DEFAULT_PATH);
        signedFilesLocation = makeAbsoluteToHome(relativeSignedFilesLocation, SIGNED_FILES_DEFAULT_PATH);
        storeFilesLocation = makeAbsoluteToHome(relativeStoreFilesLocation, STORE_FILES_DEFAULT_PATH);
    }

    private static String makeAbsoluteToHome(final String relativePath, final String defaultPath) {
        String path = relativePath != null ? relativePath : defaultPath;
        if (!path.startsWith("/")) {
            String userHome = System.getProperty("user.home");
            path = userHome + "/" + path;
        }
        final File f = new File(path);
        if (!f.exists()) {
            f.mkdirs();
        }

        LOGGER.log(Level.CONFIG, "***** Please put your files in {0}", f.getAbsolutePath());
        return f.getAbsolutePath();
    }

    private Config() {
    }
}
