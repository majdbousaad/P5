package org.oos.bank;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oos.bank.exceptions.*;
import org.oos.bank.serializable.TransactionJSON;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class PrivateBankTest {
    PrivatBank privatBank1;
    Random random = new Random();
    int zahl;
    List<Transaction> transactions = new ArrayList<>();
    Gson gson;
    @BeforeEach
    void setup() throws BankAttributException, IOException {
        deleteDirectory(new File("./data"));
        privatBank1 = new PrivatBank("sparkasse", 0.2, 0.3);

        try {
            for (int i  =0; i < 100; i++){
                transactions.add(
                        new Payment(generateString(),
                                Math.random() * 1000 + 1,
                                generateString(), Math.random(),
                                Math.random()
                        ));
                transactions.add(new IncomingTransfer(generateString(),
                        Math.abs(Math.random() * 1000 + 1),
                        generateString(),
                        generateString(),
                        generateString()
                ));
                transactions.add(new OutgoingTransfer(generateString(),
                        Math.abs(Math.random() * 1000 + 1),
                        generateString(),
                        generateString(),
                        generateString()
                ));

            }
        } catch (TransactionAttributeException e){
            throw new RuntimeException(e);
        }
        zahl = random.nextInt(transactions.size());

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeHierarchyAdapter(Transaction.class, new TransactionJSON());
        gsonBuilder.setPrettyPrinting();
        gson = gsonBuilder.create();
    }

    void deleteDirectory(File directory){
        if(directory.isDirectory()) {
            File[] files = directory.listFiles();

            // if the directory contains any file
            if(files != null) {
                for(File file : files) {

                    // recursive call if the subdirectory is non-empty
                    deleteDirectory(file);
                }
            }
        }

        if(directory.delete()) {
            System.out.println(directory + " is deleted");
        }
        else {
            System.out.println("Directory not deleted");
        }
    }
    @AfterEach
    void clearData(){
        deleteDirectory(new File("./data"));
    }

    @Test
    void Konstruktor(){

        Assertions.assertEquals(privatBank1.getName(), "sparkasse");
        Assertions.assertEquals(privatBank1.getIncomingInterest(), 0.2);
        Assertions.assertEquals(privatBank1.getOutgoingInterest(), 0.3);

        Assertions.assertThrows(BankAttributException.class, () -> {
           new PrivatBank("sparkasse",
                   4,
                   0.2);
        });
        Assertions.assertThrows(BankAttributException.class, () -> {
            new PrivatBank("sparkasse",
                    0.7,
                    4);
        });



        try {
            File testIOExceptionData = new File("./data/Bank");
            testIOExceptionData.mkdirs();

            File account = new File("./data/Bank/account.json");
            Payment payment = new Payment("test", 50, "test", 0.2, 0.3);
            FileWriter fileWriter = new FileWriter(account);
            fileWriter.write(gson.toJson(Arrays.asList(payment)));
            fileWriter.flush();
            Assertions.assertThrows(IOException.class, () -> {
                new PrivatBank("Bank", 0.2, 0.4);
            });
            fileWriter.close();
            account.delete();

            account.createNewFile();
            payment.setIncomingInterest(0.2);
            payment.setOutgoingInterest(0.4);
            fileWriter = new FileWriter(account);
            fileWriter.write(gson.toJson(Arrays.asList(payment)));

            fileWriter.flush();
            fileWriter.close();
            Assertions.assertDoesNotThrow(() -> {
                new PrivatBank("Bank", 0.2, 0.4);
            });

            account.delete();

            account.createNewFile();

            fileWriter = new FileWriter(account);
            fileWriter.write(gson.toJson(Arrays.asList(payment, payment)));

            fileWriter.flush();
            fileWriter.close();

            Assertions.assertThrows(IOException.class, () -> {
                new PrivatBank("Bank", 0.2, 0.4);
            });



        } catch (TransactionAttributeException | IOException e) {
            throw new RuntimeException(e);
        }


    }

    @Test
    void createAccountWithNoTransactions()
            throws
            AccountAlreadyExistsException {

        // adding an account
        privatBank1.createAccount("Majd");
        Assertions.assertEquals(new ArrayList<>(), privatBank1.getTransactions("Majd"));

        // adding an existing account
        Assertions.assertThrows(AccountAlreadyExistsException.class, () -> privatBank1.createAccount("Majd"));

    }

    @Test
    void createAccountWithTransactions()
            throws
            TransactionAlreadyExistException,
            TransactionAttributeException {


        try {
            // adding an account
            privatBank1.createAccount("Majd");

        } catch (AccountAlreadyExistsException  e) {
            throw new RuntimeException(e);
        }

        Assertions.assertThrows(AccountAlreadyExistsException.class, () -> {
           privatBank1.createAccount("Majd", transactions, false);
        });

        try {
            privatBank1.createAccount("Dany", transactions, false);
        } catch (AccountAlreadyExistsException e) {
            throw new RuntimeException(e);
        }
        List<Transaction> transactionList = privatBank1.getTransactions("Dany");
        Transaction transaction;
        for (int i = random.nextInt(transactions.size()/2); i < transactions.size(); i++){
            transaction = transactionList.get(i);
            if(transaction instanceof Payment payment){
                Assertions.assertEquals(payment.getIncomingInterest(), privatBank1.getIncomingInterest());
                Assertions.assertEquals(payment.getOutgoingInterest(), privatBank1.getOutgoingInterest());
            }
        }


    }

    @Test
    void addTransaction()
            throws TransactionAlreadyExistException,
            AccountDoesNotExistException,
            TransactionAttributeException,
            AccountAlreadyExistsException{

        int randomTrasaction = random.nextInt(transactions.size());
        Transaction transaction = transactions.get(randomTrasaction);

        String dasKonto = null;
        if(transaction instanceof IncomingTransfer incomingTransfer){
            privatBank1.createAccount(incomingTransfer.getSender());
            dasKonto = incomingTransfer.getRecipient();

        } else if (transaction instanceof OutgoingTransfer outgoingTransfer){
            privatBank1.createAccount(outgoingTransfer.getRecipient());
            dasKonto = outgoingTransfer.getSender();
        }

        privatBank1.createAccount(dasKonto);
        privatBank1.addTransaction(dasKonto, transaction);
        Assertions.assertEquals(privatBank1.getTransactions(dasKonto).get(0), transaction);

        String finalDasKonto = dasKonto;
        Assertions.assertThrows(TransactionAlreadyExistException.class, () -> {
           privatBank1.addTransaction(finalDasKonto, transaction);
        });
        Assertions.assertThrows(AccountDoesNotExistException.class, () -> {
           privatBank1.addTransaction("Dany", transaction);
        });


    }

    @Test
    void removeTransaction() throws AccountDoesNotExistException, TransactionDoesNotExistException, TransactionAttributeException, TransactionAlreadyExistException, AccountAlreadyExistsException {

        privatBank1.createAccount("Majd", transactions, false);
        Transaction transaction = transactions.get(zahl);
        Assertions.assertThrows(AccountDoesNotExistException.class, () -> privatBank1.removeTransaction("Petra",transaction));

        Assertions.assertThrows(TransactionDoesNotExistException.class, () -> {
            privatBank1.removeTransaction("Majd", new Payment("Test", 40, "test", 0.1, 0.2));
        });

        List<Transaction> transactionList = new ArrayList<>(privatBank1.getTransactions("Majd"));

        for (Transaction transaction1 : transactionList){
            privatBank1.removeTransaction("Majd", transaction1);
        }

        Assertions.assertEquals(new ArrayList<>(), privatBank1.getTransactions("Majd"));

    }

    @Test
    void containsTransaction() throws TransactionAlreadyExistException, AccountAlreadyExistsException, TransactionAttributeException {

        privatBank1.createAccount("Majd", transactions, false);
        Assertions.assertTrue(privatBank1.containsTransaction("Majd", transactions.get(zahl)));
        transactions.get(zahl).setDate("as5345345345d");
        Assertions.assertFalse(privatBank1.containsTransaction("Majd", transactions.get(zahl)));

    }

    @Test
    void getAccountBalance() throws AccountDoesNotExistException, TransactionAlreadyExistException, AccountAlreadyExistsException, TransactionAttributeException {
        privatBank1.createAccount("Majd", transactions, false);
        privatBank1.createAccount("Dany");
        double sum = 0;
        for (Transaction transaction : transactions){
            sum += transaction.calculate();
        }
        Assertions.assertEquals(
                sum,
                privatBank1.getAccountBalance("Majd"));

        Assertions.assertEquals(
                0,
                privatBank1.getAccountBalance("Dany"));
    }

    @Test
    void getTransactionsSorted() throws TransactionAttributeException, TransactionAlreadyExistException, AccountAlreadyExistsException {
        privatBank1.createAccount("Majd", transactions, false);

        privatBank1.createAccount("Dany");


        List<Transaction> transactionSorted=privatBank1.getTransactionsSorted("Majd", true);
        transactions.sort((  t1, t2) -> Double.compare(t1.calculate(), t2.calculate()));

        Assertions.assertEquals(transactions, transactionSorted);

        transactionSorted = privatBank1.getTransactionsSorted("Majd", false);
        transactions.sort((  t1, t2) -> Double.compare(t2.calculate(), t1.calculate()));
        Assertions.assertEquals(transactions, transactionSorted);

        List<Transaction> transactionSortedDany=privatBank1.getTransactionsSorted("Dany", true);
        Assertions.assertEquals(new ArrayList<>(), transactionSortedDany);

    }

    @Test
    void equals() throws BankAttributException, IOException {

        PrivatBank privatBank2 = new PrivatBank("test", 0.4, 0.5);
        Assertions.assertNotEquals(privatBank1, null);
        Assertions.assertEquals(privatBank1, privatBank1);

        privatBank2.setIncomingInterest(privatBank1.getIncomingInterest());
        Assertions.assertNotEquals(privatBank1, privatBank2);

        privatBank2.setOutgoingInterest(privatBank1.getOutgoingInterest());
        Assertions.assertNotEquals(privatBank1, privatBank2);

        privatBank2.setName(privatBank1.getName());

        Assertions.assertEquals(privatBank1, privatBank2);
        try {
            privatBank1.createAccount("Majd");
        } catch (AccountAlreadyExistsException ignored) {

        }
        Assertions.assertNotEquals(privatBank1,privatBank2);

        try {
            privatBank2.createAccount("Majd");
        } catch (AccountAlreadyExistsException ignored) {

        }
        Assertions.assertEquals(privatBank1,privatBank2);

        PrivatBank fvs = new PrivatBank(privatBank2);

        Assertions.assertNotEquals(fvs, privatBank2);

    }

    @Test
    void TOSTRING() throws TransactionAlreadyExistException, AccountAlreadyExistsException, TransactionAttributeException {
        String s = "PrivatBank{" +
                "name='" + privatBank1.getName() + '\'' +
                ", incomingInterest=" + privatBank1.getIncomingInterest() +
                ", outgoingInterest=" + privatBank1.getOutgoingInterest() +
                ", accountsToTransactions=" + new HashMap<>() +
                '}';

        Assertions.assertEquals(s, privatBank1.toString());

        privatBank1.createAccount("Majd", transactions, false);

        HashMap<String, List<Transaction>> map = new HashMap<>();
        map.put("Majd", privatBank1.getTransactions("Majd"));

        s = "PrivatBank{" +
                "name='" + privatBank1.getName() + '\'' +
                ", incomingInterest=" + privatBank1.getIncomingInterest() +
                ", outgoingInterest=" + privatBank1.getOutgoingInterest() +
                ", accountsToTransactions=" + map +
                '}';
        Assertions.assertEquals(s, privatBank1.toString());
    }

    @Test
    void getTransactionsByType() throws TransactionAlreadyExistException, AccountAlreadyExistsException, TransactionAttributeException {
        privatBank1.createAccount("Majd", transactions, false);


        List<Transaction> transactionTyped=privatBank1.getTransactionsByType("Majd", true);

        List<Transaction>transactionList = transactions.stream().filter(transaction -> transaction.calculate() > 0).collect(Collectors.toList());

        Assertions.assertEquals(transactionTyped, transactionList);

         transactionTyped=privatBank1.getTransactionsByType("Majd", false);

        transactionList = transactions.stream().filter(transaction -> transaction.calculate() < 0).collect(Collectors.toList());

        Assertions.assertEquals(transactionTyped, transactionList);
    }
    private String generateString(){
        return RandomStringUtils.randomAlphabetic(10);
    }
}
