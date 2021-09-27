package com.linkare.assinare.daemon;

import java.awt.HeadlessException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.linkare.assinare.daemon.gui.AssinareDaemonGui;
import com.linkare.assinare.daemon.handler.daemon.InfoHandler;
import com.linkare.assinare.daemon.handler.daemon.ServerInfo;
import com.linkare.assinare.daemon.handler.daemon.ShutdownHandler;
import com.linkare.assinare.daemon.handler.id.CitizenAddressHandler;
import com.linkare.assinare.daemon.handler.id.CitizenDataHandler;
import com.linkare.assinare.daemon.handler.id.CitizenPictureHandler;
import com.linkare.assinare.daemon.handler.signature.LocalFilesHandler;
import com.linkare.assinare.daemon.handler.signature.SignAsicsHandler;
import com.linkare.assinare.daemon.handler.signature.SignPdfHandler;
import com.linkare.assinare.daemon.net.AssinareServer;
import com.linkare.assinare.id.AssinareId;

/*
 * source:
 * http://www.rgagnon.com/javadetails/java-have-a-simple-http-server.html
 */
public final class AssinareDaemon {

    private static final int INET_SOCKET_ADDRESS_PORT = 20666;
    private static final Logger LOG = Logger.getLogger(AssinareDaemon.class.getName());

    private final AssinareServer server;

    public static void main(String[] args) throws HeadlessException, IOException, GeneralSecurityException {
        AssinareDaemon.createAssinareDaemon(INET_SOCKET_ADDRESS_PORT, new AssinareId());
    }

    private static AssinareDaemon createAssinareDaemon(final int port, final AssinareId assinareIdMain)
            throws IOException, HeadlessException, GeneralSecurityException {
        final AssinareDaemon ad = new AssinareDaemon(port);

        // Signature
        ad.getServer().setEndPoint("/sign/pdf", new SignPdfHandler());
        ad.getServer().setEndPoint("/sign/container", new SignAsicsHandler());
        ad.getServer().setEndPoint("/sign/localFiles", new LocalFilesHandler());
        // Id
        ad.getServer().setEndPoint("/id/data", new CitizenDataHandler(assinareIdMain));
        ad.getServer().setEndPoint("/id/address", new CitizenAddressHandler(assinareIdMain));
        ad.getServer().setEndPoint("/id/picture", new CitizenPictureHandler(assinareIdMain));
        // Daemon
        ad.getServer().setEndPoint("/info", new InfoHandler(ad));
        ad.getServer().setEndPoint("/shutdown", new ShutdownHandler(ad));

        ad.getServer().setExecutor(null); // creates a default executor
        ad.getServer().start();

        AssinareDaemonGui.createDaemonGui(ad);

        LOG.log(Level.INFO, "Assinare Daemon init done");
        LOG.log(Level.INFO, () -> ad.getServerInfo().toString());
        return ad;
    }

    private AssinareDaemon(final int port) throws IOException, GeneralSecurityException {
        this.server = new AssinareServer(port);
    }

    private AssinareServer getServer() {
        return this.server;
    }

    public void shutdown() {
        this.server.stop();
        AssinareDaemonGui.closeGui();
        LOG.log(Level.INFO, "Server stop signal received.");
    }

    public ServerInfo getServerInfo() {
        return this.server.getServerInfo();
    }
}
