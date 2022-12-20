package org.oos.bank;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.oos.bank.exceptions.*;
import org.oos.bank.serializable.TransactionJSON;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class PrivatBank implements  Bank{

    /**
     * Name der Bank
     */
    private String name;


    private Gson gson;
    /**
     * Zinsen [0, 1] bei einer Einzahlung
     */
    private double incomingInterest;

    /**
     * Zinsen [0, 1] bei einer Auszahlung
     */
    private double outgoingInterest;

    /**
     * Transaktionen eines Accounts.
     * String: Name des Kontos
     */
    private Map<String, List<Transaction>> accountsToTransactions = new HashMap<>();

    /**
     *
     * @param name Name der Bank
     * @param incomingInterest Zinsen [0, 1] bei einer Einzahlung
     * @param outgoingInterest Zinsen [0, 1] bei einer Auszahlung
     * @throws BankAttributException if the validation check for certain attributes fail
     */
    public PrivatBank(String name, double incomingInterest, double outgoingInterest) throws BankAttributException, IOException {
        this.name = name;
        setIncomingInterest(incomingInterest);
        setOutgoingInterest(outgoingInterest);

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Payment.class, new TransactionJSON());
        gsonBuilder.registerTypeAdapter(IncomingTransfer.class, new TransactionJSON());
        gsonBuilder.registerTypeAdapter(OutgoingTransfer.class, new TransactionJSON());
        gsonBuilder.registerTypeAdapter(Transaction.class, new TransactionJSON());
        gsonBuilder.setPrettyPrinting();
        this.gson = gsonBuilder.create();

        readAccounts();

    }

    /**
     *
     * @param privatBank bank to be constricted from
     */
    public PrivatBank(PrivatBank privatBank) {
        this.name = privatBank.name;
        this.incomingInterest = privatBank.incomingInterest;
        this.outgoingInterest = privatBank.outgoingInterest;

    }

    /**
     *
     * @param o class to be compared with
     * @return true if banks are equal (all parameters), false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PrivatBank that = (PrivatBank) o;
        return Double.compare(that.incomingInterest, incomingInterest) == 0
                && Double.compare(that.outgoingInterest, outgoingInterest) == 0
                && name.equals(that.name)
                && accountsToTransactions.equals(that.accountsToTransactions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, incomingInterest, outgoingInterest, accountsToTransactions);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getIncomingInterest() {
        return incomingInterest;
    }

    /**
     *
     * @param incomingInterest   [0, 1] bei einer Einzahlung
     * @throws BankAttributException if the validation check for certain attributes fail
     */
    public void setIncomingInterest(double incomingInterest) throws BankAttributException{
        if(incomingInterest < 0 || incomingInterest > 1){
            throw new BankAttributException(" Bank incomingInterest liegt nicht in [0,1] : " + incomingInterest);
        }
        this.incomingInterest = incomingInterest;
    }

    public double getOutgoingInterest() {
        return outgoingInterest;
    }

    /**
     *
     * @param outgoingInterest   [0, 1] bei einer Auszahlung
     * @throws BankAttributException if the validation check for certain attributes fail
     */

    public void setOutgoingInterest(double outgoingInterest) throws BankAttributException {
        if(outgoingInterest < 0 || outgoingInterest > 1){
            throw new BankAttributException("Bank outgoingInterest liegt nicht in [0,1] : " + outgoingInterest);
        }
        this.outgoingInterest = outgoingInterest;
    }

    @Override
    public String toString() {
        return "PrivatBank{" +
                "name='" + name + '\'' +
                ", incomingInterest=" + incomingInterest +
                ", outgoingInterest=" + outgoingInterest +
                ", accountsToTransactions=" + accountsToTransactions +
                '}';
    }

    @Override
    public void deleteAccount(String account) throws AccountDoesNotExistException, IOException {
        if(!accountsToTransactions.containsKey(account)){
            throw new AccountDoesNotExistException("account " + account + " existiert nicht");
        }
        accountsToTransactions.remove(account);
        File file = new File("./data/"+ name + "/" + account + ".json");
        if(file.delete()) {
            System.out.println(file + " is deleted");
        }
        else {
            System.out.println(file + " not deleted");
        }
    }

    @Override
    public List<String> getAllAccounts() {
        return accountsToTransactions.keySet().stream().toList();
    }

    /**
     * Adds an account to the bank.
     *
     * @param account the account to be added
     * @throws AccountAlreadyExistsException if the account already exists
     */
    @Override
    public void createAccount(String account) throws AccountAlreadyExistsException {
        if(accountsToTransactions.containsKey(account)){
            throw new AccountAlreadyExistsException("Konto '" + account + "' existiert schon");
        }
        accountsToTransactions.put(account, new ArrayList<>());

        try {
            writeAccount(account);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Adds an account (with specified transactions) to the bank.
     * Important: duplicate transactions must not be added to the account!
     *
     * @param account      the account to be added
     * @param transactions a list of already existing transactions which should be added to the newly created account
     * @throws AccountAlreadyExistsException    if the account already exists
     * @throws TransactionAlreadyExistException if the transaction already exists
     * @throws TransactionAttributeException    if the validation check for certain attributes fail
     * @throws AccountDoesNotExistException     if one of senders or recipients by a transaction do not exist
     */
    @Override
    public void createAccount(String account, List<Transaction> transactions, boolean fromFile)
            throws AccountAlreadyExistsException,
            TransactionAlreadyExistException,
            TransactionAttributeException
            {
        if(accountsToTransactions.containsKey(account)){
            throw new AccountAlreadyExistsException("Konto '" + account + "' existiert schon");
        }


        createAccount(account);

        for (Transaction transaction : transactions){
            if(containsTransaction(account, transaction)){
                throw new TransactionAlreadyExistException("Transaction existiert schon:\n" + transaction.toString());
            }
            if(transaction instanceof Payment payment){
                if(fromFile) {
                    if(payment.getIncomingInterest() != incomingInterest
                            || payment.getOutgoingInterest() != outgoingInterest){
                        throw new TransactionAttributeException("Payment Incoming and Outgoing interest are not compatible with the Bank");
                    }
                }
                else {
                    payment.setIncomingInterest(incomingInterest);
                    payment.setOutgoingInterest(outgoingInterest);
                    }


                accountsToTransactions.get(account).add(new Payment(payment));

            } else if(transaction instanceof IncomingTransfer incomingTransfer) {
                accountsToTransactions.get(account).add(new IncomingTransfer(incomingTransfer));

            } else if(transaction instanceof OutgoingTransfer outgoingTransfer){
                accountsToTransactions.get(account).add(new OutgoingTransfer(outgoingTransfer));

            }
        }
        try {
            writeAccount(account);
        }catch (IOException e){

        }




    }

    private void checkIfGoodTransaction(String account, Transaction transaction)
            throws TransactionAttributeException, AccountDoesNotExistException{

        if(transaction instanceof Transfer){

            String sender = ((Transfer) transaction).getSender();
            String recipient = ((Transfer) transaction).getRecipient();

            if(!accountsToTransactions.containsKey(sender) ) {
                throw new AccountDoesNotExistException("Account " + sender + " existiert nicht");
            }
            if(!accountsToTransactions.containsKey(recipient)){
                throw new AccountDoesNotExistException("Account " + recipient + " existiert nicht");
            }

            if((!sender.equals(account) && !recipient.equals(account))) {
                throw new TransactionAttributeException("Account " + account + " ist weder Sender noch Empfänger");
            }
            if(sender.equals(account) && recipient.equals(account)){
                throw new TransactionAttributeException("Sender und Empfänger können nicht gleich sein: account name : " + account);
            }

            if(transaction instanceof IncomingTransfer){
                if(!recipient.equals(account)){
                    throw new TransactionAttributeException("IncomingTransfers Empfänger ist nicht der Kontoinhaber " + recipient + " != " + account);
                }
            }else {
                if(!sender.equals(account)){
                    throw new TransactionAttributeException("OutgoingTransfers Sender ist nicht der Kontoinhaber " + sender + " != " + account);
                }
            }
        }

    }
    /**
     * Adds a transaction to an already existing account.
     *
     * @param account     the account to which the transaction is added
     * @param transaction the transaction which should be added to the specified account
     * @throws TransactionAlreadyExistException if the transaction already exists
     * @throws AccountDoesNotExistException     if the specified account does not exist
     * @throws TransactionAttributeException    if the validation check for certain attributes fail
     */
    @Override
    public void addTransaction(String account, Transaction transaction)
            throws TransactionAlreadyExistException,
            AccountDoesNotExistException,
            TransactionAttributeException {
        if(!accountsToTransactions.containsKey(account)){
            throw new AccountDoesNotExistException("Account " + account + " existiert nicht");
        }
        if(containsTransaction(account, transaction)){
            throw new TransactionAlreadyExistException("Transaction existiert schon:\n" + transaction.toString());
        }
        checkIfGoodTransaction(account, transaction);

        if(transaction instanceof Payment payment){
            payment.setIncomingInterest(this.incomingInterest);
            payment.setOutgoingInterest(this.outgoingInterest);
            accountsToTransactions.get(account).add(payment);
            try {
                writeAccount(account);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        } else {

            if(transaction instanceof IncomingTransfer incomingTransfer) {
                accountsToTransactions.get(account).add(incomingTransfer);
                String dasAndereKonto = incomingTransfer.getSender();
                accountsToTransactions.get(dasAndereKonto).add(new OutgoingTransfer(incomingTransfer));
                try {
                    writeAccount(account);
                    writeAccount(dasAndereKonto);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            } else if(transaction instanceof OutgoingTransfer outgoingTransfer){

                accountsToTransactions.get(account).add(outgoingTransfer);

                String dasAndereKonto = outgoingTransfer.getRecipient();


                accountsToTransactions.get(dasAndereKonto).add(new IncomingTransfer(outgoingTransfer));
                try {
                    writeAccount(account);
                    writeAccount(dasAndereKonto);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }




        }


    }

    /**
     * Removes a transaction from an account. If the transaction does not exist, an exception is
     * thrown.
     *
     * @param account     the account from which the transaction is removed
     * @param transaction the transaction which is removed from the specified account
     * @throws AccountDoesNotExistException     if the specified account does not exist
     * @throws TransactionDoesNotExistException if the transaction cannot be found
     */
    @Override
    public void removeTransaction(String account, Transaction transaction)
            throws AccountDoesNotExistException,
            TransactionDoesNotExistException
             {
        if(!accountsToTransactions.containsKey(account)){
            throw new AccountDoesNotExistException("Account " + account + " existiert nicht");
        }
        if(!containsTransaction(account, transaction)){
            throw new TransactionDoesNotExistException("Transaction existiert nicht:\n" + transaction.toString());
        }


        if(transaction instanceof IncomingTransfer){
            accountsToTransactions.get(account).remove(transaction);
            /*
            try {
                accountsToTransactions.get(((Transfer) transaction).getSender()).remove(new OutgoingTransfer((Transfer) transaction) );
            } catch (TransactionAttributeException ignored) {
            }

           */

        } else if(transaction instanceof OutgoingTransfer){
            accountsToTransactions.get(account).remove(transaction);
            /*
            try {
                accountsToTransactions.get(((Transfer) transaction).getRecipient()).remove(new IncomingTransfer((Transfer) transaction));
            } catch (TransactionAttributeException ignored) {
            }

             */

        } else {

            try {
                Payment payment = new Payment((Payment) transaction);
                payment.setIncomingInterest(incomingInterest);
                payment.setOutgoingInterest(outgoingInterest);
                accountsToTransactions.get(account).remove(transaction);
            } catch (TransactionAttributeException ignored) {
            }


        }

        try {
            writeAccount(account);
        } catch (IOException e){

        }


    }

    /**
     * Checks whether the specified transaction for a given account exists.
     *
     * @param account     the account from which the transaction is checked
     * @param transaction the transaction to search/look for
     */
    @Override
    public boolean containsTransaction(String account, Transaction transaction){
        if(transaction instanceof Payment){
            try {
                Payment payment = new Payment((Payment) transaction);
                payment.setIncomingInterest(incomingInterest);
                payment.setOutgoingInterest(outgoingInterest);
                return  accountsToTransactions.get(account).contains(payment);

            } catch (TransactionAttributeException e) {
                throw new RuntimeException(e);
            }

        } else if(transaction instanceof IncomingTransfer incomingTransfer) {
            return accountsToTransactions.get(account).contains(incomingTransfer);

        } else if(transaction instanceof OutgoingTransfer outgoingTransfer){
            return accountsToTransactions.get(account).contains(outgoingTransfer );

        }

        return false;
    }

    /**
     * Calculates and returns the current account balance.
     *
     * @param account the selected account
     * @throws AccountDoesNotExistException     if the specified account does not exist
     * @return the current account balance
     */
    @Override
    public double getAccountBalance(String account) throws AccountDoesNotExistException{
        if(!accountsToTransactions.containsKey(account)){
            throw new AccountDoesNotExistException("Account " + account + " existiert nicht");
        }

        List<Transaction> transactions = getTransactions(account);
        double balance = 0.0;
        for (Transaction transaction : transactions){

            balance += transaction.calculate();

        }

        return balance;
    }

    /**
     * Returns a list of transactions for an account.
     *
     * @param account the selected account
     * @return the list of all transactions for the specified account
     */
    @Override
    public List<Transaction> getTransactions(String account) {
        return accountsToTransactions.get(account);
    }

    /**
     * Returns a sorted list ( calculated amounts) of transactions for a specific account. Sorts the list either in ascending or descending order
     * (or empty).
     *
     * @param account the selected account
     * @param asc     selects if the transaction list is sorted in ascending or descending order
     * @return the sorted list of all transactions for the specified account
     */
    @Override
    public List<Transaction> getTransactionsSorted(String account, boolean asc) {
        if(asc)
            accountsToTransactions.get(account).sort((  t1, t2) -> Double.compare(t1.calculate(), t2.calculate()));
        else
            accountsToTransactions.get(account).sort(( t1, t2) -> Double.compare(t2.calculate(), t1.calculate()));

        return accountsToTransactions.get(account);
    }

    /**
     * Returns a list of either positive or negative transactions ( calculated amounts).
     *
     * @param account  the selected account
     * @param positive selects if positive or negative transactions are listed
     * @return the list of all transactions by type
     */
    @Override
    public List<Transaction> getTransactionsByType(String account, boolean positive) {
        if(positive) {
            return accountsToTransactions.get(account).stream().filter(transaction -> transaction.calculate()>0).collect(Collectors.toList());
        }else {
            return accountsToTransactions.get(account).stream().filter(transaction -> transaction.calculate()<0).collect(Collectors.toList());
        }

    }

    /**
     * Diese Methode soll alle vorhandenen Konten vom Dateisy-stem lesen und im PrivateBank-Objekt (genauer: im Klassenattribut accountsToTransactions) zur Ver-fügung stellen
     *
     * @throws IOException wenn IO Fehler auftaucht
     */
    private void readAccounts() throws IOException {

        File folder = new File("./data/" + name);

        File[] listOfFiles = folder.listFiles();

        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                if (file.isFile() && file.getName().endsWith(".json")) {
                    Reader reader = Files.newBufferedReader(Paths.get("./data/"+name+"/" + file.getName()));
                    List<Transaction> transactionList =
                            gson.fromJson(reader, new TypeToken<List<Transaction>>() {
                            }.getType());
                    try {
                        createAccount(new String(file.getName().substring(0, file.getName().indexOf("."))),

                                transactionList, true);
                    } catch (Exception e){
                        throw new IOException(e);
                    }


                }
            }
        }

    }

    /**
     * Diese Methode soll das angegebene Konto im Dateisystem persistieren (serialisieren und anschließend speichern).
     * @param account der Konto
     * @throws IOException wenn IO Fehler auftaucht
     */
    private void writeAccount(String account)throws IOException {
        File f = new File("./data/" + name);
        if(f.mkdirs()){
            System.out.println("directory " + name + " created");
        }

        FileWriter writer = new FileWriter("./data/" + name + "/" + account + ".json");

        gson.toJson(accountsToTransactions.get(account), writer);
        writer.flush();
        writer.close();

        System.out.println("File " + account + ".json created");

    }
}
