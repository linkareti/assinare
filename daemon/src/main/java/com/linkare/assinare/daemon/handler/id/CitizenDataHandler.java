package com.linkare.assinare.daemon.handler.id;

import org.json.JSONObject;

import com.linkare.assinare.commons.AssinareError;
import com.linkare.assinare.commons.AssinareException;
import com.linkare.assinare.id.AssinareId;

public class CitizenDataHandler extends AssinareBaseDataHandler {

    private final AssinareId assinareId;

    public CitizenDataHandler(final AssinareId assinareId) {
        this.assinareId = assinareId;
    }

    @Override
    protected JSONObject getData() throws AssinareError, AssinareException {
        return new JSONObject(assinareId.getCitizenData());
    }
}
