package com.codve.nio;

public class Server {

    public static void main(String[] args) {
        int port = 8080;
        MultipleExerTimeServer timeServer = new MultipleExerTimeServer(port);
        new Thread(timeServer, "nio-multipleExerTimeServer-001").start();
    }
}
