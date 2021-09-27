package com.linkare.assinare.daemon.handler.daemon;

import com.linkare.assinare.daemon.AssinareDaemon;
import com.linkare.assinare.daemon.net.AssinareBaseHandler;
import com.sun.net.httpserver.HttpExchange;

public class InfoHandler extends AssinareBaseHandler {

    private final AssinareDaemon ad;

    public InfoHandler(AssinareDaemon ad) {
        super(HTTP_METHOD_GET);
        this.ad = ad;
    }

    @Override
    protected Object handleMainMethod(HttpExchange t) {
        return ad.getServerInfo();
    }
}
