package id1212.homework2.hangmanServer.filehandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public final class FileHandler {

    private final String path = "words.txt";
    private String[] wordArrays;

    public FileHandler() {
        try {
            readTextFile(path);
            getRandomWord();
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    public String getRandomWord() {
        return wordArrays[(int) (Math.random() * wordArrays.length)].toLowerCase();
    }

    private void readTextFile(String fileName) throws IOException {
        List<String> list = Files.readAllLines(Paths.get(fileName));
        wordArrays = list.toArray(new String[0]);
    }
}
