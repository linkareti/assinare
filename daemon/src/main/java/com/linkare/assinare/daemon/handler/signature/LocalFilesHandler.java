package com.linkare.assinare.daemon.handler.signature;

import org.json.JSONArray;

import com.linkare.assinare.commons.AssinareError;
import com.linkare.assinare.commons.AssinareException;
import com.linkare.assinare.daemon.handler.id.AssinareBaseDataHandler;
import com.linkare.assinare.sign.AssinareSign;

public class LocalFilesHandler extends AssinareBaseDataHandler {

    private final AssinareSign assinareMain = new AssinareSign();

    @Override
    protected JSONArray getData() throws AssinareError, AssinareException {
        return new JSONArray(assinareMain.chooseLocalFiles());
    }
}
