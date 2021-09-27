package com.linkare.assinare.daemon.handler.daemon;

import com.linkare.assinare.daemon.AssinareDaemon;
import com.linkare.assinare.daemon.net.AssinareBaseHandler;
import com.sun.net.httpserver.HttpExchange;

public class ShutdownHandler extends AssinareBaseHandler {

    private static final String INFO_MSG = "Assinare Daemon stop.";

    private final AssinareDaemon ad;

    public ShutdownHandler(AssinareDaemon ad) {
        super(HTTP_METHOD_POST);
        this.ad = ad;
    }

    @Override
    protected Object handleMainMethod(HttpExchange t) {
        return new ServerInfo(INFO_MSG);
    }

    @Override
    protected void afterSuccessResponseSent() {
        ad.shutdown();
    }
}
