package com.codve.block;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    public static void main(String[] args) {
        String host = "127.0.0.1";
        int port = 8080;
        try (Socket client = new Socket(host, port)) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                 PrintWriter writer = new PrintWriter(client.getOutputStream(), true)) {
                writer.println("what's the time?");
                String response = reader.readLine();
                System.out.println("response is: " + response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
