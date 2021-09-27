package com.linkare.assinare.web;

import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author bnazare
 */
public class GetFile extends AbstractGetFileServlet {

    private static final long serialVersionUID = 1678338386395787156L;

    @Override
    protected Path calculateFilePath(HttpServletRequest request, String filename) {
        return Paths.get(
                Config.getOriginalFilesLocation(),
                filename
        );
    }
}
