package org.oos.controllers;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import org.oos.bank.Payment;
import org.oos.bank.PrivatBank;
import org.oos.bank.Transaction;
import org.oos.bank.exceptions.AccountDoesNotExistException;
import org.oos.bank.exceptions.TransactionAlreadyExistException;
import org.oos.bank.exceptions.TransactionAttributeException;

import java.util.Map;

public class Account  {

    private PrivatBank privatBank;

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
    private Button backButton;

    @FXML
    private MenuButton filterButton, neueButton;


    @FXML
    public void passData(PrivatBank privatBank, String selected) throws TransactionAttributeException, TransactionAlreadyExistException, AccountDoesNotExistException {
        this.privatBank = privatBank;
        this.name = selected;

        kontoName.setText(name);
        try {
            kontoBalance.setText(String.valueOf(privatBank.getAccountBalance(kontoName.getText())) + '€');
        } catch (AccountDoesNotExistException e) {
            throw new RuntimeException(e);
        }

        transactionsList.addAll(privatBank.getTransactions(name));
        transaktionenListView.setItems(transactionsList);
    }
}
