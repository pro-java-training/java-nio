package com.codve.aio;

public class Server {

    public static void main(String[] args) {
        ServerHandler timeServer = new ServerHandler(8888);
        new Thread(timeServer, "server").start();
    }
}
