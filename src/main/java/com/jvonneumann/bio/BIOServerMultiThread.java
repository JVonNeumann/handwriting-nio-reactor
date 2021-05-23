package com.jvonneumann.bio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * BIO multi thread 模式
 * <p>
 * 多线程BIOServer支持并发连接
 * 本质上仍有两处阻塞：监听客户端连接、读写客户端数据
 * 由于读写数据在另一个新线程中进行，因此对主线程而言读写客户端数据为非阻塞，但是对处理读写数据的线程而言也是阻塞模式
 */
public class BIOServerMultiThread {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket();
        serverSocket.bind(new InetSocketAddress(9090));
        System.out.println("服务端监听在9090端口");
        while (true) {
            Socket socket = serverSocket.accept(); //阻塞1：监听客户端连接
            new Thread(new Runnable() {//new thread: 处理客户端数据
                @Override
                public void run() {
                    System.out.println("收到客户端连接：" + socket.getRemoteSocketAddress());
                    try {
                        byte[] recvData = new byte[1024];
                        socket.getInputStream().read(recvData); // 阻塞2：读取客户端数据
                        System.out.println("收到客户端消息：" + new String(recvData).trim());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }
}
