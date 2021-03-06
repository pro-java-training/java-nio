package com.codve.aio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.concurrent.CountDownLatch;

public class Server extends Thread{

    AsynchronousServerSocketChannel server;

    public CountDownLatch latch;

    public Server(int port) {
        try {
            server = AsynchronousServerSocketChannel.open();
            server.bind(new InetSocketAddress(port));
            System.out.println("server is started in: " + port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        latch = new CountDownLatch(1);
        // 第二个参数用于处理 accept 事件
        server.accept(this, new AcceptHandler());
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        new Server(8888).start();
    }
}
