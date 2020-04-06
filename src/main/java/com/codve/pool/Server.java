package com.codve.pool;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Server {
    public static void main(String[] args) {
        int port = 8080;
        try (ServerSocket server = new ServerSocket(port)) {
            ThreadPoolExecutor executor = new ThreadPoolExecutor(4, 8, 120L, TimeUnit.SECONDS,
                    new ArrayBlockingQueue<>(64));;
            while (true) {
                Socket client = server.accept();
                executor.execute(new Task(client));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
