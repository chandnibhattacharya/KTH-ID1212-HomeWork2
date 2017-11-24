package id1212.homework2.hangmanServer.gameplay;

import id1212.homework2.hangmanServer.filehandler.FileHandler;

public class HangmanPlay {

    private final FileHandler file = new FileHandler();
    public final String randomWord = file.getRandomWord();
    public String underline = "";
    private int count = 0;

    public int getCount() {
        return count;
    }

    public String getUnderline() {
        for (int i = 0; i < randomWord.length(); i++) {
            underline += "_";
        }
        return underline;
    }

    public int updateUnderline(char guess) {
        String wordFound = "";
        int state = 0;
        for (int i = 0; i < randomWord.length(); i++) {
            if (randomWord.charAt(i) == guess) {
                wordFound += guess;
            } else if (underline.charAt(i) != '_') {
                wordFound += randomWord.charAt(i);
            } else {
                wordFound += "_";
            }
        }

        if (underline.equals(wordFound)) {
            count++;
            state = 1;
            if (count == 7) {
                state = 2;
            }
        } else {
            underline = wordFound;
        }
        if (underline.equals(randomWord)) {
            state = 3;
        }
        return state;
    }
}
