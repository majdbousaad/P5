package org.oos.controllers;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.oos.Main;
import org.oos.bank.*;
import org.oos.bank.exceptions.*;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

public class Account  {

    private PrivatBank privatBank;

    private Map<String, Object> data;

    private String name;

    @FXML
    private Text kontoBalance, kontoName;

    @FXML
    private GridPane content;

    @FXML
    private ListView<Transaction> transaktionenListView;

    @FXML
    private ObservableList<Transaction> transactionsList = FXCollections.observableArrayList();

    @FXML
    private MenuButton filterButton, neueButton;


    @FXML
    public void passData(Map<String, Object> data) throws TransactionAttributeException, TransactionAlreadyExistException, AccountDoesNotExistException {
        this.data = data;

        setUp();

    }

    @FXML
    void setUp(){
        this.privatBank = (PrivatBank) data.get("bank");
        this.name = (String) data.get("account");

        kontoName.setText(name);
        try {
            kontoBalance.setText(String.valueOf(privatBank.getAccountBalance(kontoName.getText())) + '€');
        } catch (AccountDoesNotExistException e) {
            throw new RuntimeException(e);
        }

        transactionsList.addAll(privatBank.getTransactions(name));
        transaktionenListView.setItems(transactionsList);
    }

    public void toMainView(ActionEvent event) {
        Scene scene = (Scene) data.get("scene");
        Stage stage = (Stage) data.get("stage");
        stage.setScene(scene);
    }

    public void aufsteigend(ActionEvent event) {
        transactionsList.setAll(privatBank.getTransactionsSorted(name, true));
    }

    public void absteigend(ActionEvent event) {
        transactionsList.setAll(privatBank.getTransactionsSorted(name, false));

    }

    public void positiv(ActionEvent event) {
        transactionsList.setAll(privatBank.getTransactionsByType(name, true));

    }

    public void negativ(ActionEvent event) {
        transactionsList.setAll(privatBank.getTransactionsByType(name, false));

    }

    public void newPayment(ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("neuesPayment.fxml"));
        Dialog<DialogPane> dialog = new Dialog<>();
        DialogPane dialogPane = fxmlLoader.load();
        dialogPane.setExpandableContent(null);
        dialog.setDialogPane(dialogPane);

        final Button btOk = (Button) dialog.getDialogPane().lookupButton(ButtonType.FINISH);
        btOk.addEventFilter(ActionEvent.ACTION, event1 -> {


            try {
                String datum = ((TextField) dialogPane.lookup("#neuesDatum")).getText();
                double betrag = Double.parseDouble(((TextField) dialogPane.lookup("#neuerBetrag")).getText());
                String besschreibung = ((TextField) dialogPane.lookup("#neueBeschreibung")).getText();
                double incomingInterest = Double.parseDouble(((TextField) dialogPane.lookup("#neuesIncomingInterest")).getText());
                double outcomingInterest = Double.parseDouble(((TextField) dialogPane.lookup("#neuesOutcomingInterest")).getText());
                Payment payment = new Payment(
                        datum,
                        betrag,
                        besschreibung,
                        incomingInterest,
                        outcomingInterest
                );
                privatBank.addTransaction(name, payment);
                kontoBalance.setText(String.valueOf(privatBank.getAccountBalance(kontoName.getText())) + '€');

                transactionsList.add(payment);

            }
            catch (TransactionAlreadyExistException | TransactionAttributeException | NumberFormatException e) {
                    ((Text) dialogPane.lookup("#error")).setText(e.getMessage());
                event1.consume();
            } catch (AccountDoesNotExistException ignored) {

            }
        });

        dialog.showAndWait();
    }

    public void newTransfer(ActionEvent event) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("neuesTransfer.fxml"));
        Dialog<DialogPane> dialog = new Dialog<>();
        DialogPane dialogPane = fxmlLoader.load();
        dialogPane.setExpandableContent(null);
        dialog.setDialogPane(dialogPane);

        final Button btOk = (Button) dialog.getDialogPane().lookupButton(ButtonType.FINISH);
        btOk.addEventFilter(ActionEvent.ACTION, event1 -> {


            try {
                String datum = ((TextField) dialogPane.lookup("#neuesDatum")).getText();
                double betrag = Double.parseDouble(((TextField) dialogPane.lookup("#neuerBetrag")).getText());
                String besschreibung = ((TextField) dialogPane.lookup("#neueBeschreibung")).getText();
                String neuerSender = (((TextField) dialogPane.lookup("#neuerSender")).getText());
                String neuerRecipient = (((TextField) dialogPane.lookup("#neuerRecipient")).getText());
                Transfer transfer;
                if(Objects.equals(neuerRecipient, name)){
                    transfer = new IncomingTransfer(
                            datum,
                            betrag,
                            besschreibung,
                            neuerSender,
                            neuerRecipient
                    );
                } else {
                    transfer = new OutgoingTransfer(
                            datum,
                            betrag,
                            besschreibung,
                            neuerSender,
                            neuerRecipient
                    );
                }

                privatBank.addTransaction(name, transfer);

                transactionsList.add(transfer);
                kontoBalance.setText(String.valueOf(privatBank.getAccountBalance(kontoName.getText())) + '€');

            } catch (TransactionAlreadyExistException | AccountDoesNotExistException | TransactionAttributeException |
                     NumberFormatException e) {
                ((Text) dialogPane.lookup("#error")).setText(e.getMessage());
                event1.consume();
            }

        });

        dialog.showAndWait();
    }

    public void deleteTransaction(ActionEvent event) throws AccountDoesNotExistException, TransactionDoesNotExistException {
        Transaction transaction = transaktionenListView.getSelectionModel()
                .getSelectedItem();

        privatBank.removeTransaction(name, transaction);
        transactionsList.remove(transaction);
        kontoBalance.setText(String.valueOf(privatBank.getAccountBalance(kontoName.getText())) + '€');

    }
}
