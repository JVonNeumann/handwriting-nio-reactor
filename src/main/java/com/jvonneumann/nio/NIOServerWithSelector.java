package com.jvonneumann.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * Java的NIO模式的Selector网络通讯，其实就是一个简单的Reactor模型。可以说是Reactor模型的朴素原型
 *
 * 1. 基于事件驱动 -> selector（支持对多个socketChannel的监听）。
 * 2. 统一的事件分派中心 -> dispatch。
 * 3. 事件处理服务 -> read & write。
 * 4. NIO一定程度解决了BIO的同步阻塞和连接限制问题。
 * 5. NIO的一个重要特点：socket`读`、`写`、`注册`和`接收`函数，在等待就绪阶段都是非阻塞的，真正的I/O操作是同步阻塞的（消耗CPU但性能非常高）。
 * 6. 如果有大量文件描述符都要读，那么就得一个一个的read。这会带来大量的Context Switch（ read 是系统调用，每调用一次就得在用户态和核心态切换一次）。
 * */
public class NIOServerWithSelector {

    private Selector selector;

    public void start() throws IOException {
        selector = Selector.open();
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(9090));
        serverSocketChannel.configureBlocking(false);

        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (!Thread.interrupted()) {
            selector.select(); //阻塞等待，只获取有事件的socket
            Set selected = selector.selectedKeys(); //事件列表
            Iterator iterator = selected.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = (SelectionKey) iterator.next();
                iterator.remove();
                dispatch(key); //分发事件
            }
        }
        if (selector != null) {
            selector.close();
        }
    }

    private void dispatch(SelectionKey key) throws IOException {
        if (key.isAcceptable()) {
            register(key); //注册新建立的连接
        } else if (key.isReadable()) {
            read(key); //读事件处理
        } else if (key.isWritable()) {
            write(key); //写事件处理
        }
    }

    private void register(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        //获得与客户端连接的通道
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);

        //客户端通道注册到selector
        socketChannel.register(selector, SelectionKey.OP_READ);
    }

    private void read(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

        // 如果有大量文件描述符都读取，那么就得一个一个的read
        // 这会带来大量的Context Switch（ read 是系统调用，每调用一次就得在用户态和核心态切换一次）
        // 这是 JAVA NIO 的主要问题
        int length = 0;
        while ((length = socketChannel.read(byteBuffer)) > 0) {
            byteBuffer.flip();
            String recvData = new String(byteBuffer.array(), 0, length).trim();
            byteBuffer.clear();
            System.out.println("Received data from client " + socketChannel.getRemoteAddress() + ": " + recvData);
        }
        //向客户端发送响应报文
        socketChannel.register(this.selector, SelectionKey.OP_WRITE);
    }

    private void write(SelectionKey key) throws IOException {
        String sendData = "I am server";
        byte[] bytes = sendData.getBytes();
        //分配一个bytes的length长度的ByteBuffer
        ByteBuffer write = ByteBuffer.allocate(bytes.length);
        //将返回数据写入缓冲区
        write.put(bytes);
        write.flip();

        //将缓冲数据写入渠道，返回给客户端
        SocketChannel socketChannel = (SocketChannel) key.channel();
        socketChannel.write(write);

        socketChannel.close();//向客户端写完响应报文后，关闭当前通道
    }

    public static void main(String[] args) throws IOException {
        new NIOServerWithSelector().start();
    }
}
