package com.codve.aio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;

public class ClientHandler implements CompletionHandler<Void, ClientHandler>, Runnable {

    private AsynchronousSocketChannel client;
    private int port;
    private CountDownLatch latch;

    public ClientHandler(int port) {
        this.port = port;
        try {
            client = AsynchronousSocketChannel.open();
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    @Override
    public void run() {
        latch = new CountDownLatch(1);
        client.connect(new InetSocketAddress(port), this, this);
        try {
            latch.await();
            client.close();
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void completed(Void result, ClientHandler attachment) {
        byte[] request = "what's the time?".getBytes(StandardCharsets.UTF_8);
        ByteBuffer writeBuffer = ByteBuffer.allocate(request.length);
        writeBuffer.put(request);
        writeBuffer.flip();
        client.write(writeBuffer, writeBuffer, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer result, ByteBuffer buffer) {
                if (buffer.hasRemaining()) {
                    client.write(buffer, buffer, this);
                } else {
                    ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                    client.read(readBuffer, readBuffer, new CompletionHandler<Integer, ByteBuffer>() {
                        @Override
                        public void completed(Integer result, ByteBuffer buffer) {
                            buffer.flip();
                            byte[] bytes = new byte[buffer.remaining()];
                            buffer.get(bytes);
                            String response = new String(bytes, StandardCharsets.UTF_8);
                            System.out.println("response: " + response);
                            latch.countDown();
                        }

                        @Override
                        public void failed(Throwable exc, ByteBuffer attachment) {
                            try {
                                client.close();
                                latch.countDown();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
                }
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                try {
                    client.close();
                    latch.countDown();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

    }

    @Override
    public void failed(Throwable exc, ClientHandler attachment) {
        try {
            client.close();
            latch.countDown();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
