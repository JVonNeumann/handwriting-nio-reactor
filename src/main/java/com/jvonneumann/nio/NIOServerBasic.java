package com.jvonneumann.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 一个线程处理所有连接
 *
 * 1. 有可能客户端只是建立连接，还没有进行读写
 * 2. 这里每次要处理大量无读写事件发生的连接，效率太低
 */
public class NIOServerBasic {

    private static List<SocketChannel> channelList = new ArrayList<SocketChannel>();

    public static void main(String[] args) throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(9090));
        serverSocketChannel.configureBlocking(false);

        while (true) {
            SocketChannel socketChannel = serverSocketChannel.accept();
            if (socketChannel != null) {
                System.out.println("连接成功:" + socketChannel.getRemoteAddress());
                socketChannel.configureBlocking(false);
                channelList.add(socketChannel);
            }

            // 有可能客户端只是建立连接，还没有进行读写
            // 每次循环要处理大量无读写事件发生的连接，效率太低，selector？
            Iterator<SocketChannel> iterator = channelList.iterator();
            while (iterator.hasNext()) {
                SocketChannel sc = iterator.next();
                ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

                int length = sc.read(byteBuffer);
                if (length > 0) {
                    System.out.println("接收到的消息:" + new String(byteBuffer.array(), 0, length));
                } else if (length == -1) {
                    iterator.remove();
                    System.out.println("客户端断开连接");
                }
            }
        }
    }
}
