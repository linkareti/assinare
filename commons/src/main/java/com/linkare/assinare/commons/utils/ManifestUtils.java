package com.linkare.assinare.commons.utils;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author bnazare
 */
public class ManifestUtils {

    private static final Logger LOG = Logger.getLogger(ManifestUtils.class.getName());

    /**
     * Copied from
     * {@link org.graalvm.nativeimage.ImageInfo#PROPERTY_IMAGE_CODE_KEY}.
     */
    private static final String PROPERTY_IMAGE_CODE_KEY = "org.graalvm.nativeimage.imagecode";
    private static final String QUARKUS_PKG_PREFIX = "io.quarkus.";

    private ManifestUtils() {
    }

    public static boolean isCodebaseWildcard() {
        if (isExemptEnvironment()) {
            return false;
        } else {
            Manifest loadManifestFile = loadManifestFile();
            if (loadManifestFile == null) {
                //if the manifest could not be loaded apply watermark
                return true;
            } else {
                Attributes attributes = loadManifestFile.getMainAttributes();
                String appLibAllowableCodebase = attributes.getValue("Application-Library-Allowable-Codebase");
                String callerAllowableCodebase = attributes.getValue("Caller-Allowable-Codebase");

                //apply watermark if either of attributes is not present in the META-INF/MANIFEST.MF file 
                if (callerAllowableCodebase == null || appLibAllowableCodebase == null) {
                    return true;
                }

                //apply watermark if any of the two attribute is * 
                return "*".equals(callerAllowableCodebase) || "*".equals(appLibAllowableCodebase);
            }
        }
    }

    private static Manifest loadManifestFile() {
        URLClassLoader cl = (URLClassLoader) ManifestUtils.class.getClassLoader();
        try {
            URL url = cl.findResource("META-INF/MANIFEST.MF");
            return new Manifest(url.openStream());
        } catch (IOException ioex) {
            LOG.log(Level.SEVERE, "Could not open manifest file.", ioex);
        }
        return null;
    }

    private static boolean isExemptEnvironment() {
        // check if native mode
        if (System.getProperty(PROPERTY_IMAGE_CODE_KEY) != null) {
            return true;
        } else {
            // check if Quarkus-jvm mode
            String clTypeName = ManifestUtils.class.getClassLoader().getClass().getName();
            return clTypeName.startsWith(QUARKUS_PKG_PREFIX);
        }
    }

}
