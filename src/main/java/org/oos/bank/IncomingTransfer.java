package org.oos.bank;

import org.oos.bank.exceptions.TransactionAttributeException;

public class IncomingTransfer extends Transfer implements CalculateBill{


    /**
     * @param date        Dieses Attribut soll den Zeitpunkt einer Ein- oder Auszahlung bzw. einer Überweisung darstellen. Das Datumsformat soll als DD.MM.YYYY angegeben werden (dies muss nicht pro-grammatisch überprüft werden).
     * @param amount      Dieses Attribut soll die Geldmenge einer Ein- oder Auszahlung bzw. einer Überweisung darstellen.
     *                    Wichtig: Bei Payment-Objekten kann sowohl ein negativer ( Auszahlung) als auch ein positiver Wert (Einzahlung) angegeben werden; bei Transfer-Objekten sind lediglich positive Werte als Eingabe erlaubt
     * @param description Dieses Attribut erlaubt eine zusätzliche Beschreibung des Vorgangs.
     * @param sender      Dieses Attribut gibt an, welcher Akteur die Geldmenge, die in amount angege-ben wurde, überwiesen hat.
     * @param recipient   Dieses Attribut gibt an, welcher Akteur die Geldmenge, die in amount ange-geben wurde, überwiesen bekommen hat.
     * @throws TransactionAttributeException wenn zinsen nicht in [0, 1]
     */
    public IncomingTransfer(String date, double amount, String description, String sender, String recipient) throws TransactionAttributeException {
        super(date, amount, description, sender, recipient);
    }

    public IncomingTransfer(OutgoingTransfer outgoingTransfer) throws TransactionAttributeException {
        super(outgoingTransfer);

    }

    /**
     * Compy-Constructor
     *
     * @param transfer to be constructed from
     * @throws TransactionAttributeException wenn zinsen nicht in [0, 1]
     */
    public IncomingTransfer(Transfer transfer) throws TransactionAttributeException {
        super(transfer);
    }

    /**
     * @return the same amount
     */
    @Override
    public double calculate() {
        return super.calculate();
    }


}
