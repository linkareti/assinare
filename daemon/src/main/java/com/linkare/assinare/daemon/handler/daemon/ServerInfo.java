package com.linkare.assinare.daemon.handler.daemon;

import org.json.JSONObject;

/**
 *
 * @author bnazare
 */
public class ServerInfo extends JSONObject {
    
    private static final String KEY_INFO = "info";
    private static final String KEY_HOST_IP_ADDRESS = "hostIPAddress";
    private static final String KEY_HOST_NAME = "hostName";
    private static final String KEY_HOST_PORT = "hostPort";

    public ServerInfo(String info) {
        put(KEY_INFO, info);
    }
    
    public ServerInfo(String info, String hostIPAddress, String hostName, int hostPort) {
        put(KEY_INFO, info);
        put(KEY_HOST_IP_ADDRESS, hostIPAddress);
        put(KEY_HOST_NAME, hostName);
        put(KEY_HOST_PORT, hostPort);
    }
    
    public String getInfo() {
        return optString(KEY_INFO);
    }
    
    public String getHostIPAddress() {
        return optString(KEY_HOST_IP_ADDRESS);
    }
    
    public String getHostName() {
        return optString(KEY_HOST_NAME);
    }
    
    public Integer getHostPort() {
        if (has(KEY_HOST_PORT)) {
            return getInt(KEY_HOST_PORT);
        } else {
            return null;
        }
    }
    
}
