package com.codve.aio;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class AcceptHandler implements CompletionHandler<AsynchronousSocketChannel, ServerHandler> {

    @Override
    public void completed(AsynchronousSocketChannel result, ServerHandler attachment) {
        attachment.server.accept(attachment, this); // 继续处理下一个 accept 事件
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        // 第一个参数用于接收数据, 第三个参数回调 Handler, 第二个参数作为 Handler 的入参
        result.read(buffer, buffer, new ReadHandler(result));
    }

    @Override
    public void failed(Throwable exc, ServerHandler attachment) {
        System.out.println(exc.getMessage());
        attachment.latch.countDown();
    }
}
