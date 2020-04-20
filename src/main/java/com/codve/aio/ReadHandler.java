package com.codve.aio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ReadHandler implements CompletionHandler<Integer, ByteBuffer> {

    private AsynchronousSocketChannel channel;

    public ReadHandler(AsynchronousSocketChannel channel) {
        this.channel = channel;
    }

    @Override
    public void completed(Integer result, ByteBuffer attachment) {
        attachment.flip();
        byte[] body = new byte[attachment.remaining()];
        attachment.get(body);
        String request = new String(body, StandardCharsets.UTF_8);
        System.out.println("received request: " + request);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String msg = LocalDateTime.now().format(formatter);
        doWrite(msg);
    }

    @Override
    public void failed(Throwable exc, ByteBuffer attachment) {
        try {
            channel.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void doWrite(String msg) {
        if (msg == null || msg.trim().length() == 0) {
            return;
        }
        byte[] bytes = msg.getBytes(StandardCharsets.UTF_8);
        ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);
        writeBuffer.put(bytes);
        writeBuffer.flip();
        channel.write(writeBuffer, writeBuffer, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer result, ByteBuffer buffer) {
                // 如果没有发送完成, 继续发送
                if (buffer.hasRemaining()) {
                    channel.write(buffer, buffer, this);
                }
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                try {
                    channel.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
