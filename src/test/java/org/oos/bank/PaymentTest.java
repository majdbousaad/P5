package org.oos.bank;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oos.bank.exceptions.TransactionAttributeException;

public class PaymentTest {

    Payment paymentPositiv;
    Payment paymentNegativ;

    @BeforeEach
    void setup() throws TransactionAttributeException{
        paymentPositiv = new Payment("test"
                , 40
                ,"test"
                ,0.2, 0.3);

        paymentNegativ = new Payment("test"
                , -40
                ,"test"
                ,0.2, 0.3);
    }
    @Test
    void konstruktor() throws TransactionAttributeException{

        Assertions.assertEquals("test", paymentPositiv.getDate());
        Assertions.assertEquals(40, paymentPositiv.getAmount());
        Assertions.assertEquals("test", paymentPositiv.description);
        Assertions.assertEquals(0.2, paymentPositiv.getIncomingInterest());
        Assertions.assertEquals(0.3, paymentPositiv.getOutgoingInterest());

        Assertions.assertThrows(TransactionAttributeException.class, () -> {
            new Payment("test"
                    , 40
                    ,"test"
                    ,6, 0.3);
        });

        Assertions.assertThrows(TransactionAttributeException.class, () -> {
            new Payment("test"
                    , 40
                    ,"test"
                    ,0.7, 3);
        });


    }

    @Test
    void kopyKonstruktor() throws TransactionAttributeException {
        Payment payment1 = new Payment(paymentPositiv);

        Assertions.assertEquals("test", payment1.getDate());
        Assertions.assertEquals(40, payment1.getAmount());
        Assertions.assertEquals("test", payment1.description);
        Assertions.assertEquals(0.2, payment1.getIncomingInterest());
        Assertions.assertEquals(0.3, payment1.getOutgoingInterest());
    }

    @Test
    void calculate(){
        Assertions.assertEquals(paymentPositiv.calculate(),
                paymentPositiv.getAmount()
                        - paymentPositiv.getAmount()
                        *paymentPositiv.getIncomingInterest()
        );

        Assertions.assertEquals(paymentNegativ.calculate(),
                paymentNegativ.getAmount()
                        + paymentNegativ.getAmount()
                        *paymentNegativ.getOutgoingInterest()
        );
    }

    @Test
    void equals() throws TransactionAttributeException {
        Assertions.assertNotEquals(paymentPositiv, paymentNegativ);
        Assertions.assertNotEquals(paymentPositiv, 5);
        Assertions.assertNotEquals(paymentPositiv, null);
        paymentNegativ.setAmount(paymentPositiv.getAmount());
        Assertions.assertEquals(paymentPositiv, paymentNegativ);
    }

    @Test
    void TOSTTRING(){
        Assertions.assertEquals(
                paymentPositiv.toString(),
                "Transaction{date='test', amount="+paymentPositiv.calculate()+", description='test', incomingInterest=0.2, outgoingInterest=0.3}"
        );

        Assertions.assertEquals(
                paymentNegativ.toString(),
                "Transaction{date='test', amount="+paymentNegativ.calculate()+", description='test', incomingInterest=0.2, outgoingInterest=0.3}"
        );

    }

}
