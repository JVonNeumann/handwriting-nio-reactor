package com.jvonneumann;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress("127.0.0.1", 9090));
        System.out.println("成功连接服务端");

        Scanner scanner = new Scanner(System.in);
        String sendData = scanner.nextLine();
        socket.getOutputStream().write(sendData.getBytes());
    }
}
