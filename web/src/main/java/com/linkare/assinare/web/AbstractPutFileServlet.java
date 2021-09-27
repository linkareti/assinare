package com.linkare.assinare.web;

import static javax.servlet.http.HttpServletResponse.SC_NO_CONTENT;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

/**
 *
 * @author Bruno Nazaré - Linkare TI
 */
public abstract class AbstractPutFileServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(AbstractPutFileServlet.class.getName());
    private static final long serialVersionUID = -7160442992518757669L;

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setStatus(SC_NO_CONTENT);
        try {
            boolean isMultipart = ServletFileUpload.isMultipartContent(request);
            if (isMultipart) {
                final FileItemFactory factory = new DiskFileItemFactory();
                final ServletFileUpload upload = new ServletFileUpload(factory);
                final List<FileItem> items = upload.parseRequest(request);
                final FileItem myFileItem = items.get(0);

                Path targetPath = getTargetPath(request, myFileItem.getName());
                Files.createDirectories(targetPath.getParent());
                Files.write(targetPath, myFileItem.get());
            } else {
                LOGGER.log(Level.WARNING, "Request was not a multipart request.");
            }
        } catch (FileUploadException ex) {
            Logger.getLogger(AbstractPutFileServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    protected abstract Path getTargetPath(final HttpServletRequest request, final String filename);

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
