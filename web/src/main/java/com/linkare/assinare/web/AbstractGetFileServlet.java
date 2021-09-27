package com.linkare.assinare.web;

import static com.linkare.assinare.web.Utils.getUTF8Parameter;
import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

/**
 *
 * @author bnazare
 * @author ralves
 */
public abstract class AbstractGetFileServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(AbstractGetFileServlet.class.getName());
    private static final long serialVersionUID = 1458638991828844287L;

    protected static final String PARAMETER_NAME = "name";
    protected static final MimetypesFileTypeMap MIME_TYPES = new MimetypesFileTypeMap();

    static {
        MIME_TYPES.addMimeTypes("application/pdf pdf PDF");
    }

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        final String fileName = getUTF8Parameter(request, PARAMETER_NAME);
        final Path filePath = calculateFilePath(request, fileName);

        String sessEnabled = (String) request.getSession().getAttribute("hasDocAccess");

        if (Boolean.valueOf(sessEnabled)) {
            try (InputStream fis = Files.newInputStream(filePath);
                    OutputStream out = response.getOutputStream()) {
                response.setContentType(MIME_TYPES.getContentType(fileName));
                response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

                IOUtils.copy(fis, out);
            } catch (NoSuchFileException fnfex) {
                LOGGER.log(Level.FINE, null, fnfex);
                response.sendError(SC_NOT_FOUND);
            }
        } else {
            response.sendError(SC_FORBIDDEN);
        }
    }

    protected abstract Path calculateFilePath(final HttpServletRequest request, final String filename);

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
