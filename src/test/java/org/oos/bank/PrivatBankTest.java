
package org.oos.bank;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oos.bank.exceptions.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class PrivatBankTest {

    private  PrivatBank sparkasse, deutscheBank;
    private List<Transaction> transactions;

    private IncomingTransfer incomingTransfer;
    private OutgoingTransfer outgoingTransfer;
    private Payment payment;

    private void reset() {
        try {

            incomingTransfer = new IncomingTransfer(
                    "05.11.2022",
                    30,
                    "no description",
                    "Dany",
                    "Majd"
            );

            outgoingTransfer = new OutgoingTransfer(
                    "06.11.2022",
                    30,
                    "no description",
                    "Majd",
                    "Dany"
            );

            payment = new Payment(
                    "20.11.2022",
                    -200,
                    "No description",
                    0.2,
                    0.6
            );
        } catch (TransactionAttributeException e) {
            throw new RuntimeException(e);
        }
        transactions = new ArrayList<>(Arrays.asList(incomingTransfer, outgoingTransfer, payment));
    }

    @BeforeEach
    void setUp() throws BankAttributException, TransactionAlreadyExistException, AccountAlreadyExistsException, AccountDoesNotExistException, TransactionAttributeException, IOException {
        sparkasse = new PrivatBank("sparkasse", 0.4, 0.5);
        deutscheBank = new PrivatBank("deutscheBank", 0.3, 0.2);

        reset();

    }


    /*
    @Test
    void removeTransaction() throws AccountDoesNotExistException, TransactionDoesNotExistException, TransactionAttributeException {

        try {
            sparkasse.createAccount("Dany");
            sparkasse.createAccount("Majd", transactions);

        }catch ( AccountAlreadyExistsException | TransactionAlreadyExistException ignored){

        }

        Assertions.assertThrows(AccountDoesNotExistException.class, () -> sparkasse.removeTransaction("Petra", incomingTransfer));

        Assertions.assertThrows(TransactionDoesNotExistException.class, () -> {
            incomingTransfer.setRecipient("ahduf");
            sparkasse.removeTransaction("Majd", incomingTransfer);
        });

        reset();

        sparkasse.removeTransaction("Majd", incomingTransfer);
        sparkasse.removeTransaction("Majd", outgoingTransfer);
        sparkasse.removeTransaction("Majd", payment);
        Assertions.assertEquals(new ArrayList<>(), sparkasse.getTransactions("Majd"));




    }

    @Test
    void containsTransaction() {
        reset();
        try {
            sparkasse.createAccount("Dany");
            sparkasse.createAccount("Majd", transactions);

        }catch (AccountAlreadyExistsException | TransactionAlreadyExistException | TransactionAttributeException
                 ignored){
        }
        Assertions.assertTrue(sparkasse.containsTransaction("Majd", incomingTransfer));
        incomingTransfer.setDate("asd");
        Assertions.assertFalse(sparkasse.containsTransaction("Majd", incomingTransfer));




    }

    @Test
    void getAccountBalance() throws AccountDoesNotExistException{
        reset();
        try {
            sparkasse.createAccount("Dany");
            sparkasse.createAccount("Majd", transactions);

        }catch (AccountAlreadyExistsException | TransactionAlreadyExistException | TransactionAttributeException
                 ignored){
        }

        Assertions.assertEquals(
                incomingTransfer.calculate()
                        + outgoingTransfer.calculate() +
                        payment.getAmount() +
                        sparkasse.getOutgoingInterest() * payment.getAmount(),
                sparkasse.getAccountBalance("Majd"));

        Assertions.assertEquals(
                0,
                sparkasse.getAccountBalance("Dany"));
    }

    @Test
    void getTransactionsSorted() throws TransactionAttributeException {

        try {
            sparkasse.createAccount("Dany");
            sparkasse.createAccount("Majd", transactions);
        } catch (TransactionAlreadyExistException | AccountAlreadyExistsException  |
                 TransactionAttributeException ignored) {
        }
        try {
            payment.setIncomingInterest(sparkasse.getIncomingInterest());
            payment.setOutgoingInterest(sparkasse.getOutgoingInterest());
        } catch (TransactionAttributeException ignored) {
        }


        List<Transaction> transactionSorted=sparkasse.getTransactionsSorted("Majd", true);
        Assertions.assertEquals(Arrays.asList(payment, outgoingTransfer, incomingTransfer), transactionSorted);

        transactionSorted = sparkasse.getTransactionsSorted("Majd", false);
        Assertions.assertEquals(Arrays.asList(incomingTransfer, outgoingTransfer ,payment), transactionSorted);

        List<Transaction> transactionSortedDany=sparkasse.getTransactionsSorted("Dany", true);
        Assertions.assertEquals(Arrays.asList(new OutgoingTransfer(incomingTransfer), new IncomingTransfer(outgoingTransfer)), transactionSortedDany);

    }

    @Test
    void getTransactionsByType() {
        try {
            sparkasse.createAccount("Dany");
            sparkasse.createAccount("Majd", transactions);
        } catch (TransactionAlreadyExistException | AccountAlreadyExistsException  |
                 TransactionAttributeException ignored) {
        }
        try {
            payment.setIncomingInterest(sparkasse.getIncomingInterest());
            payment.setOutgoingInterest(sparkasse.getOutgoingInterest());
        } catch (TransactionAttributeException ignored) {
        }

        List<Transaction> transactionTyped=sparkasse.getTransactionsByType("Majd", true);
        Assertions.assertEquals(Collections.singletonList(incomingTransfer), transactionTyped);

        transactionTyped = sparkasse.getTransactionsByType("Majd", false);
        Assertions.assertEquals(Arrays.asList(outgoingTransfer ,payment), transactionTyped);

    }

    @Test
    void equals() throws BankAttributException {
        Assertions.assertNotEquals(sparkasse, deutscheBank);
        deutscheBank.setIncomingInterest(sparkasse.getIncomingInterest());
        deutscheBank.setOutgoingInterest(sparkasse.getOutgoingInterest());
        Assertions.assertNotEquals(sparkasse, deutscheBank);
        deutscheBank.setName(sparkasse.getName());
        try {
            sparkasse.createAccount("Majd");
        } catch (AccountAlreadyExistsException ignored) {

        }
        Assertions.assertNotEquals(sparkasse, deutscheBank);

        try {
            deutscheBank.createAccount("Majd");
        } catch (AccountAlreadyExistsException ignored) {

        }
        Assertions.assertEquals(sparkasse, deutscheBank);

        PrivatBank fvs = new PrivatBank(deutscheBank);

        Assertions.assertNotEquals(fvs, deutscheBank);

    }

    @Test
    void negativesTransfer(){

        Assertions.assertThrows(TransactionAttributeException.class, () -> incomingTransfer.setAmount(-300));
        Assertions.assertThrows(TransactionAttributeException.class, () -> outgoingTransfer.setAmount(-300));
        Assertions.assertThrows(TransactionAttributeException.class, () ->
                new IncomingTransfer("asd", -300, "asdd", "asda", "alsjhd"));
    }

    @Test
    void bankAttributException(){
        Assertions.assertThrows(BankAttributException.class, () -> new PrivatBank("majd", 2, 3));
    }
    */
}