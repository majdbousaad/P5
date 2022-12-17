package org.oos.bank;

import org.oos.bank.exceptions.TransactionAttributeException;

import java.util.Objects;

/**
 * @author Majd Bousaad
 *
 */
public abstract class Transaction implements CalculateBill{
    protected String date;
    protected double amount;
    protected String description;

    /**
     *
     * @param date date of the transaction
     * @param amount amount of the transaction
     * @param description transaction message
     *                    @throws TransactionAttributeException wenn zinsen nicht in [0, 1]
     */
    public Transaction(String date, double amount, String description) throws TransactionAttributeException {
        this.date = date;
        setAmount(amount);
        this.description = description;
    }

    /**
     *
     * @param transaction Copy-Constructor.
     * @throws TransactionAttributeException wenn zinsen nicht in [0, 1]
     */
    public Transaction(Transaction transaction) throws TransactionAttributeException{
        this(transaction.date, transaction.amount, transaction.description);
    }

    public Transaction() {
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) throws TransactionAttributeException {
        if(this instanceof Transfer){
            if(amount < 0){
                throw new TransactionAttributeException("Amount bei einem Transfer soll nicht negativ sein: " + amount);
            }
        }
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return
                "date='" + date + '\'' +
                ", amount=" + calculate() +
                ", description='" + description + '\''
                ;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Double.compare(that.amount, amount) == 0 && Objects.equals(date, that.date) && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, amount, description);
    }
}
