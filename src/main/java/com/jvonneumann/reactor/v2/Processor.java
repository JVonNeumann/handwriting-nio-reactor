package com.jvonneumann.reactor.v2;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Processor {
    private static final ExecutorService workerThreads = Executors.newFixedThreadPool(16);

    public void process(SelectionKey selectionKey) {
        workerThreads.submit(() -> {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
            int count = socketChannel.read(buffer);
            if (count < 0) {
                socketChannel.close();
                selectionKey.cancel();
                System.out.println(socketChannel + " Read ended");
                return null;
            } else if (count == 0) {
                return null;
            }
            System.out.println(socketChannel + " Read message " + new String(buffer.array(), 0, count));
            return null;
        });
    }
}
