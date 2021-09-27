package com.linkare.assinare.server.test.resources;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.ws.Endpoint;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

/**
 *
 * @author bnazare
 */
public class SCMDTestResource implements QuarkusTestResourceLifecycleManager {

    private static final List<Endpoint> ENDPOINTS = new ArrayList<>();

    @Override
    public Map<String, String> start() {
        // server functionality automatically added by the "cxf-rt-transports-http-netty-server" jar

        int port = findAvailablePort();
        String address = "http://localhost:" + port + "/mockSCMDService";
        FakeSCMDService implementor = FakeSCMDService.INSTANCE;

        ENDPOINTS.add(Endpoint.publish(address, implementor));

        return Map.of("asn.cmd.wsdl-location", address + "?wsdl");
    }

    private int findAvailablePort() throws RuntimeException {
        try (ServerSocket s = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            return s.getLocalPort();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void stop() {
        ENDPOINTS.forEach(Endpoint::stop);
    }

}
