package com.linkare.assinare.web;

import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author ralves
 */
public class GetStoredFile extends AbstractGetFileServlet {

    private static final long serialVersionUID = -2971360423941449706L;

    @Override
    protected Path calculateFilePath(HttpServletRequest request, String filename) {
        return Paths.get(
                Config.getStoreFilesLocation(),
                filename
        );
    }
}
