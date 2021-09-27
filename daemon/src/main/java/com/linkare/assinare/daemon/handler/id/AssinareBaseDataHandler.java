package com.linkare.assinare.daemon.handler.id;

import com.linkare.assinare.commons.AssinareError;
import com.linkare.assinare.commons.AssinareException;
import com.linkare.assinare.daemon.exception.HandlingException;
import com.linkare.assinare.daemon.net.AssinareBaseHandler;
import com.sun.net.httpserver.HttpExchange;

public abstract class AssinareBaseDataHandler extends AssinareBaseHandler {

    protected AssinareBaseDataHandler() {
        super(HTTP_METHOD_POST);
    }

    @Override
    protected Object handleMainMethod(HttpExchange t) throws HandlingException {
        try {
            return getData();
        } catch (RuntimeException | AssinareException | AssinareError ex) {
            throw new HandlingException(ex);
        }
    }

    protected abstract Object getData() throws AssinareError, AssinareException;
}
