package com.linkare.assinare.web;

import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author ralves
 */
public class StoreFile extends AbstractPutFileServlet {

    private static final long serialVersionUID = -2924806396064068669L;

    @Override
    protected Path getTargetPath(final HttpServletRequest request, final String filename) {
        return Paths.get(
                Config.getStoreFilesLocation(),
                filename
        );
    }
}
