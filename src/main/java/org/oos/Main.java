package org.oos;

import org.oos.bank.IncomingTransfer;
import org.oos.bank.OutgoingTransfer;
import org.oos.bank.Payment;
import org.oos.bank.PrivatBank;
import org.oos.bank.exceptions.*;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws BankAttributException, IOException, TransactionAlreadyExistException, AccountAlreadyExistsException, AccountDoesNotExistException, TransactionAttributeException {
        PrivatBank privatBank = new PrivatBank("Sparkasse", 0.2, 0.4);


        privatBank.createAccount("God");

        for (int i = 0; i < 20; i++){
            privatBank.createAccount("Majd " + i);

            for (int j = 0; j < 50; j++){
                privatBank.addTransaction("Majd " + i, new Payment("test " + j,
                        j*286 + 200,
                        "test",
                        0.1,
                        0.2)
                );

                privatBank.addTransaction("Majd " + i, new IncomingTransfer("test " + j,
                        30 * j + 200,
                        "test",
                        "God",
                        "Majd " + i)
                );


            }
        }



    }
}