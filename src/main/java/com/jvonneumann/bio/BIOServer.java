package com.jvonneumann.bio;

import java.io.IOException;
import java.net.*;

/**
 * BIO single thread 模式
 * <p>
 * 单线程BIO无法支持并发
 * 有两处阻塞：监听客户端连接、读取客户端数据
 */
public class BIOServer {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket();
        serverSocket.bind(new InetSocketAddress(9090));
        System.out.println("服务端监听在9090端口");
        while (true) {
            Socket socket = serverSocket.accept(); //阻塞1：监听客户端连接
            System.out.println("收到客户端连接：" + socket.getRemoteSocketAddress());
            byte[] recvData = new byte[1024];
            socket.getInputStream().read(recvData); // 阻塞2：读取客户端数据
            System.out.println("收到客户端消息：" + new String(recvData).trim());
        }
    }
}
