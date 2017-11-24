package id1212.homework2.hangmanServer.net;

import id1212.homework2.hangmanServer.main.HangmanServer;
import id1212.homework2.hangmanServer.gameplay.HangmanPlay;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConnectionHandler implements Runnable {

    private HangmanPlay play;
    private String toClient;
    private static final String MSG = "\nGuess any letter in the word\n";
    private static final String AGAIN = "To play again type the word again ";
    private int state, score = 0, remainingTry = 7;
    private final Selector selector;
    private static final int SIZE = 2048;
    private String input = "";
    private boolean isIdentical = false, header = false;
    private String temp = "";
    private boolean again = false, first = true;

    public ConnectionHandler(Selector selector) {
        this.selector = selector;
        newPlayInstance();
    }

    private void newPlayInstance() {
        play = new HangmanPlay();
        toClient = play.getUnderline();
    }

    @Override
    public void run() {
        while (true) {
            try {
                int readyChannels = selector.select(200);

                if (readyChannels == 0) {
                } else {
                    Set<SelectionKey> keys = selector.selectedKeys();
                    Iterator<SelectionKey> iteratorKey = keys.iterator();
                    while (iteratorKey.hasNext()) {
                        SelectionKey key = iteratorKey.next();
                        if (key.isAcceptable()) {
                            message("acceptable");
                            acceptRequest(key);
                        } else if (key.isConnectable()) {
                            message("connctable");
                        } else if (key.isReadable()) {
                            readFromClient(key);
                        } else if (key.isWritable()) {
                            if (!header) {
                                header(key);
                            }
                            if (again) {
                                if (first) {
                                    header(key);
                                    first = false;
                                }
                                playAgain(key);
                            }
                            writeToClient(key);
                        }
                        iteratorKey.remove();
                        Thread.sleep(100);
                    }
                }

            } catch (IOException | InterruptedException ex) {
                Logger.getLogger(HangmanServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getInput() {
        return input;
    }

    private void writeToClient(SelectionKey key) throws IOException {
        if (isIdentical) {
            String result = "";
            message("writing to client");

            SocketChannel socket = null;

            while (play.getCount() < 7 && play.underline.contains("_")) {
                message("Getting guess....");
                String gues = getInput();
                if (gues.isEmpty()) {
                    break;
                }
                state = play.updateUnderline(gues.toLowerCase().charAt(0));
                switch (state) {
                    case 0:
                        result = (MSG + play.underline + remainingTry + score);
                        message(result);
                        break;
                    case 1:
                        remainingTry -= 1;
                        result = ("\nWrong guess, try again\n" + play.underline + remainingTry + score);
                        break;
                    case 2:
                        remainingTry -= 1;
                        score -= 1;
                        result = ("\nYou loose! The word was: " + play.randomWord + "\n" + AGAIN + remainingTry + score);
                        break;
                    case 3:
                        score += 1;
                        result = ("\nCongrats! You won! The word was " + play.randomWord + "\n" + AGAIN + remainingTry + score);
                        break;
                    default:
                        break;
                }
                ByteBuffer buffer = ByteBuffer.allocate(SIZE);
                buffer.put(result.getBytes());
                buffer.flip();
                socket = (SocketChannel) key.channel();
                socket.write(buffer);
                buffer.clear();
                message("Sent: " + result);
                if (isIdentical && gues.equals(temp)) {
                    break;
                }
            }
            isIdentical = false;
        }
    }

    private void header(SelectionKey key) throws IOException {
        SocketChannel socketChannel;
        System.out.println("The word is: " + play.randomWord);
        socketChannel = (SocketChannel) key.channel();
        String message = MSG + play.underline + remainingTry + score;
        ByteBuffer buffer = ByteBuffer.allocate(2048);
        buffer.put(message.getBytes());
        buffer.flip();
        socketChannel.write(buffer);
        buffer.clear();
        System.out.println("sent: " + message);
        header = true;
    }

    private void readFromClient(SelectionKey key) throws IOException {
        String clientInput = "";
        SocketChannel socket = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(SIZE);
        int dataReads = socket.read(buffer);
        while (dataReads > 0) {
            buffer.flip();
            while (buffer.hasRemaining()) {
                clientInput += Character.toString((char) buffer.get());
            }
            dataReads = socket.read(buffer);
        }
        setInput(clientInput);
        isIdentical = true;
        temp = clientInput;
        if (clientInput.equals("again")) {
            again = true;
            newPlayInstance();
        }
    }

    private void acceptRequest(SelectionKey key) throws IOException {
        ServerSocketChannel serverSock = (ServerSocketChannel) key.channel();
        SocketChannel socket = serverSock.accept();
        socket.configureBlocking(false);
        socket.register(key.selector(), SelectionKey.OP_READ);
        message("New client accepted");

    }

    private void playAgain(SelectionKey key) throws IOException {
        if (isIdentical) {
            String result = "";
            message("writing to client");

            SocketChannel socket = null;

            while (play.getCount() < 7 && play.underline.contains("_")) {
                message("Getting guess....");
                String gues = getInput();
                if (gues.isEmpty()) {
                    break;
                }

                state = play.updateUnderline(gues.toLowerCase().charAt(0));
                switch (state) {
                    case 0:
                        result = (MSG + play.underline + remainingTry + score);
                        message(result);
                        break;
                    case 1:
                        remainingTry -= 1;
                        result = ("\nWrong guess, try again\n" + play.underline + remainingTry + score);
                        break;
                    case 2:
                        remainingTry -= 1;
                        score -= 1;
                        result = ("\nYou loose! The word was: " + play.randomWord + "\n" + AGAIN + remainingTry + score);
                        break;
                    case 3:
                        score += 1;
                        result = ("\nCongrats! You won! The word was " + play.randomWord + "\n" + AGAIN + remainingTry + score);
                        break;
                    default:
                        break;
                }
                ByteBuffer buffer = ByteBuffer.allocate(SIZE);
                buffer.put(result.getBytes());
                buffer.flip();
                socket = (SocketChannel) key.channel();
                socket.write(buffer);
                buffer.clear();
                message("Sent: " + result);
                if (isIdentical && gues.equals(temp)) {
                    break;
                }
            }
            isIdentical = false;
        }
    }

    /*
    defined for the demo purpose
     */
    private void message(String msg) {
        System.out.println(">" + msg);
    }
}
