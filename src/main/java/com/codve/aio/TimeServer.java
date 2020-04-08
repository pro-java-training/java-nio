package com.codve.aio;

public class TimeServer {
    public static void main(String[] args) {
        int port = 8080;
        AsyncTimeServerHandler timeServer = new AsyncTimeServerHandler(port);
        new Thread(timeServer, "server").start();
    }
}
