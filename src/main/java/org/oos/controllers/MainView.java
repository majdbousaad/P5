package org.oos.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.oos.Main;
import org.oos.bank.PrivatBank;
import org.oos.bank.exceptions.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainView {

    PrivatBank privatBank = new PrivatBank("NierBank", 0.1, 0.2);
    @FXML
    private MenuItem neuesKontoButton, deleteKonto, showKonto;

    @FXML
    private ListView<String> accountsListView;

    @FXML
    ObservableList<String> accountsList = FXCollections.observableArrayList(privatBank.getAllAccounts());

    @FXML
    private BorderPane border;

    @FXML
    private Text status;

    @FXML
    private void initialize(){
        accountsListView.setItems(accountsList);

        accountsList.addListener((ListChangeListener<? super String>) change -> {
            while (change.next()){
                if(change.wasAdded()){
                    status.setText("Neues Konto wurde hinzugefügt");
                } else if (change.wasRemoved()){
                    status.setText("ein Konto wurde gelöscht");

                }

            }
        });
    }



    public MainView() throws BankAttributException, IOException {

    }


    public void showDialogKonto(ActionEvent event) throws IOException {


        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("NeuesKonto.fxml"));
        Dialog<DialogPane> dialog = new Dialog<>();
        DialogPane dialogPane = fxmlLoader.load();
        dialogPane.setExpandableContent(null);
        dialog.setDialogPane(dialogPane);

        final Button btOk = (Button) dialog.getDialogPane().lookupButton(ButtonType.FINISH);
        btOk.addEventFilter(ActionEvent.ACTION, event1 -> {
            String kontoName = ((TextField) dialogPane.lookup("#neuerName")).getText();

            try {
                privatBank.createAccount(kontoName);
                accountsList.add(kontoName);
                System.out.println(privatBank);
            } catch (AccountAlreadyExistsException e) {
                ((Text) dialogPane.lookup("#errorMessage")).setText("Account existiert schon");
                event1.consume();
            }
        });

        dialog.showAndWait();

    }

    public void deleteKonto(ActionEvent event) {
        String selected = accountsListView.getSelectionModel().getSelectedItem();

        DialogPane confirmation = new DialogPane();
        confirmation.setHeaderText("Bestätigung");
        confirmation.setContentText("Möchten Sie wirklich das Konto " + selected + " Löschen?");
        confirmation.setExpandableContent(null);
        confirmation.getButtonTypes().addAll(ButtonType.NO, ButtonType.YES);

        Dialog<ButtonType> confirmationDialog = new Dialog<>();
        confirmationDialog.setDialogPane(confirmation);




        confirmationDialog.showAndWait()
                .filter(response -> response == ButtonType.YES)
                .ifPresent(response -> {
                    try {
                        privatBank.deleteAccount(selected);
                        accountsList.remove(selected);
                    } catch (AccountDoesNotExistException | IOException e) {
                        throw new RuntimeException(e);
                    }
                });





    }

    public void showKonto(ActionEvent event) throws IOException {
        String selected = accountsListView.getSelectionModel().getSelectedItem();



        FXMLLoader accountSceneContent = new FXMLLoader(Main.class.getResource("AccountView.fxml"));
        Parent root = accountSceneContent.load();

        Account accountController = accountSceneContent.getController();
        try {
            accountController.passData(privatBank, selected);
        } catch (TransactionAttributeException e) {
            throw new RuntimeException(e);
        } catch (TransactionAlreadyExistException e) {
            throw new RuntimeException(e);
        } catch (AccountDoesNotExistException e) {
            throw new RuntimeException(e);
        }


        Stage stage = (Stage) border.getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Kontoübersicht");
        stage.show();


    }
}
