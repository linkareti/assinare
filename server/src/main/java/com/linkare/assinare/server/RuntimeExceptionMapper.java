package com.linkare.assinare.server;

import static com.linkare.assinare.server.ErrorCode.GENERAL_ERROR;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.linkare.assinare.server.pojo.ProcessingError;

/**
 *
 * @author bnazare
 */
@Provider
public class RuntimeExceptionMapper implements ExceptionMapper<RuntimeException> {
    
    private static final Logger LOG = Logger.getLogger(RuntimeExceptionMapper.class.getName());

    @Override
    public Response toResponse(RuntimeException e) {
        LOG.log(Level.SEVERE, "Uncaught RuntimeException", e);
        
        ProcessingError error = new ProcessingError(GENERAL_ERROR);
        return Response.serverError().entity(error).build();
    }

}
