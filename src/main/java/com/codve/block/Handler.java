package com.codve.block;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Handler extends Thread {

    public static final ThreadLocal<DateTimeFormatter> formatter =
            ThreadLocal.withInitial(() -> DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

    private Socket socket;

    public Handler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {
            String body = reader.readLine();
            System.out.println("received request: " + body);
            writer.println(LocalDateTime.now().format(formatter.get()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
