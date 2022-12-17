package org.oos.bank;

import org.oos.bank.exceptions.TransactionAttributeException;

import java.util.Objects;

/**
 * Transfer inherits from Transaction and implements calculate() methode from CalculateBill interface
 */
public class Transfer extends Transaction implements CalculateBill{


    /**
     *
     * @param date Dieses Attribut soll den Zeitpunkt einer Ein- oder Auszahlung bzw. einer Überweisung darstellen. Das Datumsformat soll als DD.MM.YYYY angegeben werden (dies muss nicht pro-grammatisch überprüft werden).
     * @param amount Dieses Attribut soll die Geldmenge einer Ein- oder Auszahlung bzw. einer Überweisung darstellen.
     * Wichtig: Bei Payment-Objekten kann sowohl ein negativer ( Auszahlung) als auch ein positiver Wert ( Einzahlung) angegeben werden; bei Transfer-Objekten sind lediglich positive Werte als Eingabe erlaubt
     * @param description Dieses Attribut erlaubt eine zusätzliche Beschreibung des Vorgangs.
     * @throws TransactionAttributeException wenn amount negativ ist
     */
    public Transfer(String date, double amount, String description)throws TransactionAttributeException {
        super(date, amount, description);
    }

    /**
     *
     * @param date Dieses Attribut soll den Zeitpunkt einer Ein- oder Auszahlung bzw. einer Überweisung darstellen. Das Datumsformat soll als DD.MM.YYYY angegeben werden (dies muss nicht pro-grammatisch überprüft werden).
     * @param amount Dieses Attribut soll die Geldmenge einer Ein- oder Auszahlung bzw. einer Überweisung darstellen.
     * Wichtig: Bei Payment-Objekten kann sowohl ein negativer ( Auszahlung) als auch ein positiver Wert ( Einzahlung) angegeben werden; bei Transfer-Objekten sind lediglich positive Werte als Eingabe erlaubt
     * @param description Dieses Attribut erlaubt eine zusätzliche Beschreibung des Vorgangs.
     * @param sender Dieses Attribut gibt an, welcher Akteur die Geldmenge, die in amount angege-ben wurde, überwiesen hat.
     * @param recipient Dieses Attribut gibt an, welcher Akteur die Geldmenge, die in amount ange-geben wurde, überwiesen bekommen hat.
     * @throws TransactionAttributeException wenn zinsen nicht in [0, 1]
     */
    public Transfer(String date, double amount, String description, String sender, String recipient) throws TransactionAttributeException{
        super(date, amount, description);
        setSender(sender);
        setRecipient(recipient);

    }

    /**
     * Compy-Constructor
     * @param transfer to be constructed from
     * @throws TransactionAttributeException wenn zinsen nicht in [0, 1]
     */
    public Transfer(Transfer transfer) throws TransactionAttributeException  {
        super(transfer);
        setSender(transfer.getSender());
        setRecipient(transfer.getRecipient());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Transfer transfer = (Transfer) o;
        return sender.equals(transfer.sender) && recipient.equals(transfer.recipient);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sender, recipient);
    }

    @Override
    public String toString() {
        return "Transfer{" +
                super.toString() +
                ", sender='" + sender + '\'' +
                ", recipient='" + recipient + '\'' +
                '}';
    }

    private String sender, recipient;

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }


    /**
     *
     * @return the same amount
     */
    @Override
    public double calculate() {
        return amount;
    }
}
