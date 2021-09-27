package com.linkare.assinare.web;

import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author bnazare
 */
public class PutFile extends AbstractPutFileServlet {

    private static final long serialVersionUID = -3410683045580743492L;

    @Override
    protected Path getTargetPath(final HttpServletRequest request, final String filename) {
        return Paths.get(
                Config.getSignedFilesLocation(),
                request.getSession().getId(),
                filename
        );
    }
}
