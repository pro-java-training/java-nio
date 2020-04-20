package com.codve.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

public class Client extends Thread {

    private String host;

    private int port;

    private Selector selector;

    private SocketChannel client;

    private volatile boolean stop;

    public Client(String host, int port, String name) {
        super(name);
        try {
            this.host = host;
            this.port = port;
            selector = Selector.open();
            client = SocketChannel.open();
            client.configureBlocking(false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void run() {
        try {
            doConnect();
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (!stop) {
            try {
                selector.select(1000);
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> it = selectedKeys.iterator();
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

    private void doConnect() throws IOException {
        if (client.connect(new InetSocketAddress(host, port))) {
            // 连接成功 注册读事件
            client.register(selector, SelectionKey.OP_READ);
            doWrite(client, "what's the time?");
        } else {
            // 向 Selector 注册连接事件, 监听服务端的 ACK 应答
            client.register(selector, SelectionKey.OP_CONNECT);
        }
    }

    private void handleInput(SelectionKey key) throws IOException {
        if (key.isValid()) {
            SocketChannel socketChannel = (SocketChannel) key.channel();
            if (key.isConnectable()) { // 处理连接事件
                if (socketChannel.finishConnect()) { // 连接成功
                    socketChannel.register(selector, SelectionKey.OP_READ); // 继续注册读事件
                    doWrite(socketChannel, "what's the time?"); // 发送消息
                } else {
                    System.exit(1); // 连接失败, 退出
                }
            }
            if (key.isReadable()) { // 有新的消息
                ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                int readBytes = socketChannel.read(byteBuffer);
                if (readBytes > 0) {
                    byteBuffer.flip();
                    byte[] bytes = new byte[byteBuffer.remaining()];
                    byteBuffer.get(bytes);
                    String body = new String(bytes, StandardCharsets.UTF_8);
                    System.out.println("response: " + body);
                    this.stop = true;
                } else if (readBytes < 0) {
                    key.cancel();
                    socketChannel.close();
                }
            }
        }
    }

    private void doWrite(SocketChannel socketChannel, String msg) throws IOException {
        byte[] response = msg.getBytes(StandardCharsets.UTF_8);
        ByteBuffer byteBuffer = ByteBuffer.allocate(response.length);
        byteBuffer.put(response);
        byteBuffer.flip();
        socketChannel.write(byteBuffer);
        if (!byteBuffer.hasRemaining()) {
            System.out.println("send request succeed.");
        }
    }

    public static void main(String[] args) {
        new Client("127.0.0.1", 8080, "client").start();
    }
}
