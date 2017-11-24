package id1212.homework2.hangmanClient.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 *
 * @author ema
 */
public class ClientConnectionHandler {

    private SocketAddress address = null;
    private SocketChannel socketChannel;
    private String guess;
    private static final int BSIZE = 2048;
    private String result;
    private int score = 0, remaining = 0;

    public ClientConnectionHandler(String host, int port) {
        try {
            address = new InetSocketAddress(host, port);
            socketChannel = SocketChannel.open(address);
        } catch (IOException ex) {
            System.err.println(ex);
        }
        message("Channel opened");
    }

    public void readFromServer() {
        message("Reading from server....");
        try {
            String message = "";
            ByteBuffer byteBuffer = ByteBuffer.allocate(BSIZE);
            message("getting socket's file");
            int bytesRead = socketChannel.read(byteBuffer);
            while (bytesRead > 0) {
                message("Byte is greater than 0");
                byteBuffer.flip();
                while (byteBuffer.hasRemaining()) {
                    message += Character.toString((char) byteBuffer.get());
                }
                bytesRead = socketChannel.read(byteBuffer);
            }
            message("Got: " + message + " message!");
            splitMessage(message);
            Thread.sleep(1000);
        } catch (IOException | InterruptedException ex) {
            System.err.println("Error.." + ex);
        }
    }

    private void splitMessage(String msg) {
        message("Splithing the message: " + msg);
        String[] part = msg.split("(?<=\\D)(?=\\d)");
        result = part[0];
        String scoreRe = part[1];
        remaining = Character.getNumericValue(scoreRe.charAt(0));
        score = Character.getNumericValue(scoreRe.charAt(1));
    }

    public void writeToServer() throws IOException {
        String message = getGuess();
        ByteBuffer buffer = ByteBuffer.allocate(BSIZE);
        buffer.put(message.getBytes());
        buffer.flip();

        while (buffer.hasRemaining()) {
            socketChannel.write(buffer);
        }
        System.out.println("Sent: " + message);
    }

    public void setGuess(String guess) {
        this.guess = guess;
    }

    public String getGuess() {
        return guess;
    }

    public int getScor() {

        return score;
    }

    public int getRemainingTries() {
        return remaining;
    }

    public String getFromServer() {

        return result;
    }

    /*
    defined for the demo purpose
     */
    private static void message(String msg) {
        System.out.println(">" + msg);
    }

    public void closeConnection() {
        try {
            message("Closing connection...");
            socketChannel.close();
        } catch (IOException ex) {
            message("Unable to close...");
        }

    }

}
