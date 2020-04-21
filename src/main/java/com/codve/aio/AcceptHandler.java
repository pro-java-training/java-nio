package com.codve.aio;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class AcceptHandler implements CompletionHandler<AsynchronousSocketChannel, Server> {

    @Override
    public void completed(AsynchronousSocketChannel result, Server attachment) {
        attachment.server.accept(attachment, this); // 继续处理下一个 accept 事件
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        // 第一个参数表示接收缓冲区
        // 第二个参数表示异步 Channel 携带的附件, 作为回调的参数
        // 第三个参数表示回调 Handler, 第二个参数作为回调 Handler 方法的入参
        result.read(buffer, buffer, new ReadHandler(result));
    }

    @Override
    public void failed(Throwable exc, Server attachment) {
        System.out.println(exc.getMessage());
        attachment.latch.countDown();
    }
}
