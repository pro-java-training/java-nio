package com.codve.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Set;

public class Server extends Thread {

    private Selector selector;

    private ServerSocketChannel server; // 用于监听所有客户端的连接

    private ThreadLocal<DateTimeFormatter> formatter = ThreadLocal.withInitial(
            () -> DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

    private volatile boolean stop;

    public Server(int port, String name) {
        super(name);
        try {
            server = ServerSocketChannel.open();
            server.configureBlocking(false); // 设置非阻塞模式
            server.socket().bind(new InetSocketAddress(port), 1024);
            selector = Selector.open();
            // 把 server 注册到 Selector 上, 监听 ACCEPT 事件.
            server.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("server is started in: " + port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void run() {
        while (!stop) {
            try {
                selector.select(1000); // 休眠时间为 1 s
                Set<SelectionKey> selectionKeySet = selector.selectedKeys();
                Iterator<SelectionKey> it = selectionKeySet.iterator();
                SelectionKey key;
                while (it.hasNext()) {
                    key = it.next();
                    it.remove();
                    try {
                        handleInput(key);
                    } catch (Exception e) {
                        if (key != null) {
                            key.cancel();
                            if (key.channel() != null) {
                                key.channel().close();
                            }
                        }
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (selector != null) {
            try {
                selector.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void terminate() {
        this.stop = true;
    }

    private void handleInput(SelectionKey key) throws IOException {
        if (key.isValid()) {
            if (key.isAcceptable()) { // 有新的客户端接入
                ServerSocketChannel server = (ServerSocketChannel) key.channel();
                SocketChannel client = server.accept(); // 完成 TCP 3 次握手, 建立物理链路
                client.configureBlocking(false); // 设置客户端为非阻塞模式
                // 把客户端注册到多路复用器上, 监听读操作, 读取客户端发送的消息
                client.register(selector, SelectionKey.OP_READ);
            }
            if (key.isReadable()) {  // 有新的消息
                SocketChannel client = (SocketChannel) key.channel();
                ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                int length = client.read(readBuffer); // 读取消息
                if (length > 0) {
                    readBuffer.flip(); // 指针回到 buffer 头部
                    byte[] bytes = new byte[readBuffer.remaining()]; // 获取 buffer 中元素数量
                    readBuffer.get(bytes); // 把 buffer 中的 数据读取到 byte[] 中.
                    String request = new String(bytes, StandardCharsets.UTF_8);
                    System.out.println("received request: " + request);
                    String response = LocalDateTime.now().format(formatter.get());
                    doWrite(client, response);
                } else if (length < 0) { // 链路关闭
                    key.cancel();
                    client.close();
                }
            }
        }
    }

    private void doWrite(SocketChannel client, String response) throws IOException {
        if (response != null && response.trim().length() > 0) {
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);
            writeBuffer.put(bytes);  // 把 byte[] 中的数据写入到 buffer 中.
            writeBuffer.flip();
            client.write(writeBuffer);  // 向客户端发送消息
        }
    }

    public static void main(String[] args) {
        new Server(8080, "server").start();
    }
}
