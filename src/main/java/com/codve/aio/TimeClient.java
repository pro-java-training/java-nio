package com.codve.aio;

public class TimeClient {

    public static void main(String[] args) {
        new Thread(new ClientHandler(8888), "client").start();
    }
}
