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

    private ThreadLocal<DateTimeFormatter> formatter = ThreadLocal.withInitial(
            () -> DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    );

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
        String msg = LocalDateTime.now().format(formatter.get());
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
        byte[] bytes = msg.getBytes(StandardCharsets.UTF_8);
        ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);
        writeBuffer.put(bytes);
        writeBuffer.flip();
        // 第一个参数表示输出缓冲区
        // 第二个参数表示异步 Channel 携带的附件, 作为回调的参数
        // 第三个参数表示回调 Handler, 第二个参数作为回调 Handler 方法的入参
        channel.write(writeBuffer, writeBuffer, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer result, ByteBuffer buffer) {
                // 如果没有发送完成, 继续发送, 这里有递归调用
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
