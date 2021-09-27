package com.linkare.assinare.web;

import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author bnazare
 * @author ralves
 */
public class GetSignedFile extends AbstractGetFileServlet {

    private static final long serialVersionUID = -598689852687314261L;

    @Override
    protected Path calculateFilePath(HttpServletRequest request, String filename) {
        return Paths.get(
                Config.getSignedFilesLocation(),
                request.getSession().getId(),
                filename
        );
    }
}
