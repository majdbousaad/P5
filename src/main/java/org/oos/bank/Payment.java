package org.oos.bank;

import org.oos.bank.exceptions.TransactionAttributeException;

import java.util.Objects;

/**
 * @author Majd Bousaad
 * @since Praktikum 2
 * Payment class inherits from Transaction class and implements the calculate method
 */
public class Payment  extends Transaction implements CalculateBill{


    /**
     *
     * @param date Dieses Attribut soll den Zeitpunkt einer Ein- oder Auszahlung bzw. einer Überweisung darstellen. Das Datumsformat soll als DD.MM.YYYY angegeben werden (dies muss nicht pro-grammatisch überprüft werden).
     * @param amount Dieses Attribut soll die Geldmenge einer Ein- oder Auszahlung bzw. einer Überweisung darstellen.
     * Wichtig: Bei Payment-Objekten kann sowohl ein negativer ( Auszahlung) als auch ein positiver Wert ( Einzahlung) angegeben werden; bei Transfer-Objekten sind lediglich positive Werte als Eingabe erlaubt
     * @param description Dieses Attribut erlaubt eine zusätzliche Beschreibung des Vorgangs.
     * @throws TransactionAttributeException    if the validation check for certain attributes fail
     */
    public Payment(String date, double amount, String description) throws TransactionAttributeException {
       super(date , amount, description);

    }

    /**
     *
     * @param date Dieses Attribut soll den Zeitpunkt einer Ein- oder Auszahlung bzw. einer Überweisung darstellen. Das Datumsformat soll als DD.MM.YYYY angegeben werden (dies muss nicht pro-grammatisch überprüft werden).
     * @param amount Dieses Attribut soll die Geldmenge einer Ein- oder Auszahlung bzw. einer Überweisung darstellen.
     * Wichtig: Bei Payment-Objekten kann sowohl ein negativer ( Auszahlung) als auch ein positiver Wert ( Einzahlung) angegeben werden; bei Transfer-Objekten sind lediglich positive Werte als Eingabe erlaubt
     * @param description Dieses Attribut erlaubt eine zusätzliche Beschreibung des Vorgangs.
     * @param incomingInterest Dieses Attribut gibt die Zinsen (positiver Wert in Prozent, 0 bis 1; dies muss programmatisch überprüft werden. Eine fehlerhafte Eingabe soll auf die Konsole geschrieben werden, System.out) an, die bei einer Einzahlung („Deposit“) anfallen.
     * @param outgoingInterest Dieses Attribut gibt die Zinsen (positiver Wert in Prozent, 0 bis 1; dies muss programmatisch überprüft werden. Eine fehlerhafte Eingabe soll auf die Konsole geschrieben werden, System.out) an, die bei einer Auszahlung („Withdrawal“) anfallen.
     * @throws TransactionAttributeException wenn zinsen nicht in [0, 1]
     */
    public Payment(String date,
                   double amount,
                   String description,
                   double incomingInterest,
                   double outgoingInterest) throws TransactionAttributeException {
        super(date, amount, description);
        setIncomingInterest(incomingInterest);
        setOutgoingInterest(outgoingInterest);

    }

    /**
     * Compy-Constructor
     * @param payment to be constructed from
     * @throws TransactionAttributeException wenn zinsen nicht in [0, 1]
     */
    public Payment(Payment payment) throws TransactionAttributeException{
        super(payment);
        try {
            setIncomingInterest(payment.incomingInterest);
            setOutgoingInterest(payment.outgoingInterest);
        } catch (TransactionAttributeException ignored){

        }

    }

    @Override
    public String toString() {
        return "Transaction{" +
                super.toString() +
                ", incomingInterest=" + incomingInterest +
                ", outgoingInterest=" + outgoingInterest +
                '}';

    }
    private double incomingInterest, outgoingInterest;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Payment payment = (Payment) o;
        return Double.compare(payment.incomingInterest, incomingInterest) == 0 && Double.compare(payment.outgoingInterest, outgoingInterest) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), incomingInterest, outgoingInterest);
    }

    public double getIncomingInterest() {
        return incomingInterest;
    }

    /**
     *
     * @param incomingInterest the value should be between 0 and 1
     * @throws TransactionAttributeException    if the validation check for certain attributes fail
     */
    public void setIncomingInterest(double incomingInterest) throws TransactionAttributeException{
        if(incomingInterest < 0 || incomingInterest > 1) {
            throw new TransactionAttributeException("IncomingInterest liegt nicht in [0,1] : " + incomingInterest);
        }
        this.incomingInterest = incomingInterest;
    }

    public double getOutgoingInterest() {
        return outgoingInterest;
    }

    /**
     *
     * @param outgoingInterest the value should be between 0 and 1
     * @throws TransactionAttributeException wenn Zinsen nicht in [0, 1]
     */
    public void setOutgoingInterest(double outgoingInterest) throws TransactionAttributeException{
        if(outgoingInterest < 0 || outgoingInterest > 1) {
            throw new TransactionAttributeException("outgoingInterest liegt nicht in [0,1] : " + outgoingInterest);
        }
        this.outgoingInterest = outgoingInterest;
    }


    /**
     *
     * @return calculated amount depending on the value of in - outcominginterest
     */
    @Override
    public double calculate() {

        return (amount >=0 )?
                amount - amount*incomingInterest
                :
                amount + amount*outgoingInterest;
    }
}
