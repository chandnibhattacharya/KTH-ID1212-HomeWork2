package id1212.homework2.hangmanServer.main;

import id1212.homework2.hangmanServer.net.ConnectionHandler;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class HangmanServer {

    private static final int PORT = 8080;

    public static void main(String... args) {
        Selector selector;
        message("Server Started....");
        try {
            ServerSocketChannel server = ServerSocketChannel.open();
            server.bind(new InetSocketAddress(PORT));
            selector = Selector.open();
            new Thread(new ConnectionHandler(selector)).start();
            while (true) {
                SocketChannel socket = server.accept();
                message("Socket channel accepted....");

                if (socket != null) {
                    socket.configureBlocking(false);
                    selector.wakeup();
                    int ops = socket.validOps();
                    socket.register(selector, ops, null);
                }
            }
        } catch (IOException ex) {
            message("Error: "+ex);
        }
    }

    /*
    defined for the demo purpose
     */
    private static void message(String msg) {
        System.out.println(">" + msg);
    }

}
