package id1212.homework2.hangmanClient.controller;

import id1212.homework2.hangmanClient.net.ClientConnectionHandler;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class HangmanClientController implements Initializable {

    public ClientConnectionHandler hang;

    @FXML
    private TextArea history;
    @FXML
    private TextField txtStatus;
    @FXML
    private TextField txtScore;
    @FXML
    private TextField txtTry;
    @FXML
    private TextField txtInput;
    @FXML
    private TextField host;
    @FXML
    private TextField port;
    @FXML
    private Button connctButton;

    @FXML
    private void connectToServer(ActionEvent event) throws IOException {
        new ConnectButtunHandler().start();

    }

    @FXML
    private void playAgainButton(ActionEvent e) {
        new Re_playGame().start();
    }

    @FXML
    private void disconnect(ActionEvent event) {
        //hang.closeConnection();
        disable();
    }

    @FXML
    private void sendWord(ActionEvent e) {
        new GameResultService().start();
    }

    private void disable() {
        history.setText("Cleaning up...");
        history.setEditable(false);
        txtStatus.setText("Diconnected");
        txtStatus.setEditable(false);
        txtScore.setText("");
        txtScore.setEditable(false);
        txtTry.setEditable(false);
        txtInput.setText("");
        txtInput.setEditable(false);
    }

    private void setUpFields() {
        txtStatus.setText("Connected to Server");
        txtStatus.setEditable(false);
        txtScore.setText("0");
        txtScore.setEditable(false);
        txtTry.setEditable(false);
        history.setEditable(false);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    private class ConnectButtunHandler extends Service<ClientConnectionHandler> {

        private ConnectButtunHandler() {
            setOnSucceeded((WorkerStateEvent event) -> {
                hang = getValue();
                setUpFields();
                hang.readFromServer();
                txtScore.setText(String.valueOf(hang.getScor()));
                txtTry.setText(String.valueOf(hang.getRemainingTries()));
                history.setText(hang.getFromServer());
            });

            setOnFailed((WorkerStateEvent e) -> {
                System.out.println("connection failed...");
            });
        }

        @Override
        protected Task<ClientConnectionHandler> createTask() {
            return new Task<ClientConnectionHandler>() {
                @Override
                protected ClientConnectionHandler call() throws Exception {
                    return new ClientConnectionHandler(txtInput.getText() , Integer.parseInt(port.getText()));
                }

            };
        }
    }

    private class GameResultService extends Service<String> {

        private int tries = 0, scor = 0;
        private boolean close = false;

        private GameResultService() {
            setOnSucceeded((WorkerStateEvent e) -> {
                String val = (String) e.getSource().getValue();
                System.out.println(val);
                txtInput.setText("");
                txtTry.setText(String.valueOf(tries));
                txtScore.setText(String.valueOf(scor));
                if (close) {
                    disable();
                }
                history.appendText(val);

            });
            setOnFailed((WorkerStateEvent e) -> {
                System.out.println("failed retrieving from server...");
            });
        }

        @Override
        protected Task<String> createTask() {

            return new Task<String>() {
                @Override
                protected String call() throws IOException, InterruptedException, ClassNotFoundException {
                    if (txtInput.getText().equalsIgnoreCase("quit")) {
                        hang.closeConnection();
                        close = true;
                    }
                    hang.setGuess(txtInput.getText());
                    hang.writeToServer();
                    hang.readFromServer();
                    String result = hang.getFromServer();
                    tries = hang.getRemainingTries();
                    scor = hang.getScor();
                    return result;
                }
            };
        }
    }

    private class Re_playGame extends Service<String> {

        private Re_playGame() {
            setOnSucceeded((WorkerStateEvent e) -> {
                String val = (String) e.getSource().getValue();
                System.out.println(val);
                txtInput.setText("");
                history.appendText(val);
            });
        }

        @Override
        protected Task<String> createTask() {
            return new Task<String>() {
                @Override
                protected String call() throws IOException, InterruptedException, ClassNotFoundException {
                    hang.setGuess(txtInput.getText());
                    String result = hang.getFromServer();
                    return result;
                }
            };
        }

    }
}
