package com.linkare.assinare.daemon.net;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import com.linkare.assinare.daemon.handler.daemon.ServerInfo;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;

public class AssinareServer {

    private static final Logger LOG = Logger.getLogger(AssinareServer.class.getName());

    private static final String KS_PROPS_FILENAME = "keystore.properties";

    private static final String KS_PATH_PROP = "assinare.daemon.keystore.path";

    private static final String KS_PASSWD_PROP = "assinare.daemon.keystore.password";

    private static final String INFO_MSG = "Assinare Daemon is here.";

    private final HttpServer server;

    public AssinareServer(int port) throws IOException, GeneralSecurityException {
        this.server = createHttpsServer(port);
        LOG.log(Level.INFO, "AssinareServer created.");
    }

    public void setEndPoint(final String path, final HttpHandler handler) {
        this.server.createContext(path, handler);
        LOG.log(Level.CONFIG, "Added enpoint for: {0}", path);
    }

    public void setExecutor(Executor executor) {
        this.server.setExecutor(executor);
    }

    public void start() {
        this.server.start();
        LOG.log(Level.INFO, "Assinare HttpServers started.");
    }

    public void stop() {
        this.server.stop(1);// one second delay to wait until exchanges have finished seems good.
        LOG.log(Level.INFO, "Assinare HttpServers stoped.");
    }

    // General implementation idea taken from http://stackoverflow.com/a/2323188
    private static HttpServer createHttpsServer(int port) throws IOException, GeneralSecurityException {
        ClassLoader cl = AssinareServer.class.getClassLoader();

        Properties keyProps = new Properties();
        keyProps.load(cl.getResourceAsStream(KS_PROPS_FILENAME));
        final String ksPath = keyProps.getProperty(KS_PATH_PROP);
        final String ksPasswd = keyProps.getProperty(KS_PASSWD_PROP);

        // initialise the keystore
        final char[] password = ksPasswd.toCharArray();
        final KeyStore ks = KeyStore.getInstance("JKS");
        final InputStream fis = cl.getResourceAsStream(ksPath);
        if (fis == null) {
            throw new IllegalStateException("Keystore for localhost not found");
        } else {
            ks.load(fis, password);
        }

        if (ks.size() < 1) {
            throw new IllegalStateException("Keystore for localhost is empty");
        }

        // setup the key manager factory
        final KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, password);

        // setup the trust manager factory
        final TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks);

        // setup the HTTPS context and parameters
        final SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        final HttpsServer httpsServer = HttpsServer.create(new InetSocketAddress(port), 0);
        httpsServer.setHttpsConfigurator(new HttpsConfigurator(sslContext));

        return httpsServer;
    }

    public ServerInfo getServerInfo() {
        final InetAddress addr = server.getAddress().getAddress();

        return new ServerInfo(
                INFO_MSG,
                addr.getHostAddress(),
                addr.getHostName(),
                server.getAddress().getPort());
    }
}
