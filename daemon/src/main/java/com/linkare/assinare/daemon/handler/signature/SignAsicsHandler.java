package com.linkare.assinare.daemon.handler.signature;

import com.linkare.assinare.commons.AssinareError;
import com.linkare.assinare.commons.AssinareException;

public class SignAsicsHandler extends AssinareBaseSignatureHandler {

    @Override
    protected String handleSignature(SignatureParameters params) throws AssinareException, AssinareError {
        return assinareMain.signContainer(params.getDocs(), params.getASiCSignatureFields());
    }
}
