package com.github.melissa.lima.message;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

public class Message {
    private Type type;
    private String address;
    private int port;
    private List<String> peerFiles;
    private String fileName;
    public enum Type{
        JOIN, LEAVE, SEARCH, UPDATE, ALIVE;
    }

    public Type getType() {
        return type;
    }

    public InetAddress getAddress() throws UnknownHostException {
        return InetAddress.getByName(address);
    }
    public int getPort() {
        return port;
    }

    public List<String> getPeerFiles() {
        return peerFiles;
    }

    public String getFileName() {
        return fileName;
    }
}

