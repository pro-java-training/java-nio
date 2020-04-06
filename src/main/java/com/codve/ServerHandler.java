package com.codve;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ServerHandler implements Runnable {

    private Socket socket;

    public ServerHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {
            while (true) {
                String body = reader.readLine();
                if (body == null) {
                    break;
                }
                System.out.println("received request: " + body);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                writer.println(LocalDateTime.now().format(formatter));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
