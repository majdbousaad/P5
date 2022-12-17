package org.oos.bank;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oos.bank.exceptions.TransactionAttributeException;

public class TransferTest {
    Transfer incoming;
    Transfer outgoing;

    Transfer transfer;

    @BeforeEach
    void setup() throws TransactionAttributeException {
        incoming = new IncomingTransfer("test"
                , 40
                ,"test"
                ,"test", "test");

        outgoing = new OutgoingTransfer("test"
                , 40
                ,"test"
                ,"test", "test");

        transfer = new Transfer("test"
                , 40
                ,"test"
                ,"test", "test");
    }
    @Test
    void konstruktor() throws TransactionAttributeException{

        Assertions.assertEquals("test", transfer.getDate());
        Assertions.assertEquals(40, transfer.getAmount());
        Assertions.assertEquals("test", transfer.description);
        Assertions.assertEquals("test", transfer.getSender());
        Assertions.assertEquals("test", transfer.getRecipient());

        Assertions.assertThrows(TransactionAttributeException.class, () -> {
            new Transfer("test"
                    , -40
                    ,"test"
                    ,"test", "0.3");
        });

        transfer = new Transfer("test"
                , 40
                ,"test");
        Assertions.assertEquals("test", transfer.getDate());
        Assertions.assertEquals(40, transfer.getAmount());
        Assertions.assertEquals("test", transfer.description);




    }

    @Test
    void kopyKonstruktor() throws TransactionAttributeException {
        Transfer transfer1 = new Transfer(transfer);

        Assertions.assertEquals("test", transfer.getDate());
        Assertions.assertEquals(40, transfer.getAmount());
        Assertions.assertEquals("test", transfer.description);
        Assertions.assertEquals("test", transfer.getSender());
        Assertions.assertEquals("test", transfer.getRecipient());
    }

    @Test
    void calculate(){
        Assertions.assertEquals(incoming.calculate(),
                incoming.getAmount()
        );

        Assertions.assertEquals(outgoing.calculate(),
                -outgoing.getAmount()
        );
    }

    @Test
    void equals() throws TransactionAttributeException {
        Assertions.assertNotEquals(transfer, incoming);
        Assertions.assertNotEquals(transfer, 5);
        Assertions.assertNotEquals(transfer, null);
        Assertions.assertEquals(transfer, transfer);
    }

    @Test
    void TOSTTRING(){
        Assertions.assertEquals(
                incoming.toString(),
                "Transfer{date='test', amount="+incoming.calculate()+", description='test', sender='test', recipient='test'}"
        );

        Assertions.assertEquals(
                outgoing.toString(),
                "Transfer{date='test', amount="+outgoing.calculate()+", description='test', sender='test', recipient='test'}"
        );

    }
}
