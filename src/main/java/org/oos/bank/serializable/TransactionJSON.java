package org.oos.bank.serializable;

import com.google.gson.*;
import org.oos.bank.*;
import org.oos.bank.exceptions.TransactionAttributeException;

import java.lang.reflect.Type;

public class TransactionJSON implements JsonSerializer<Transaction>, JsonDeserializer<Transaction> {

    @Override
    public JsonElement serialize(Transaction transaction, Type type, JsonSerializationContext jsonSerializationContext) {

        JsonObject result = new JsonObject();
        String className = null;
        JsonObject instance = new JsonObject();
        if(transaction instanceof Payment payment){
            className = "Payment";
            instance.addProperty("incomingInterest", payment.getIncomingInterest());
            instance.addProperty("outgoingInterest", payment.getOutgoingInterest());

        } else{
            instance.addProperty("sender", ((Transfer)transaction).getSender());
            instance.addProperty("recipient", ((Transfer)transaction).getRecipient());
            if (transaction instanceof IncomingTransfer) {
                className = "IncomingTransfer";
            } else {
                className = "OutgoingTransfer";
            }
        }
        instance.addProperty("date", transaction.getDate());
        instance.addProperty("amount", transaction.getAmount());
        instance.addProperty("description", transaction.getDescription());

        result.addProperty("CLASSNAME", className);
        result.add("INSTANCE", instance);

        return result;
    }
    @Override
    public Transaction deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject result = jsonElement.getAsJsonObject();


        String classname = result.get("CLASSNAME").getAsString();
        JsonObject instance = result.get("INSTANCE").getAsJsonObject();
        String date = instance.get("date").getAsString();
        double amount = instance.get("amount").getAsDouble();
        String description = instance.get("description").getAsString();
        if(classname.equals(Payment.class.getSimpleName())){
            double incomingInterest = instance.get("incomingInterest").getAsDouble();
            double outgoingInterest = instance.get("outgoingInterest").getAsDouble();
            try {
                return new Payment(
                        date,
                        amount,
                        description,
                        incomingInterest,
                        outgoingInterest
                );
            } catch (TransactionAttributeException e){
                throw new JsonParseException("incomingInterest or outgoinginterest out of bounds");
            }

        } else {

            String sender = instance.get("sender").getAsString();
            String recipient = instance.get("recipient").getAsString();
            try {
                if (classname.equals(IncomingTransfer.class.getSimpleName())){
                    return new IncomingTransfer(
                            date,
                            amount,
                            description,
                            sender,
                            recipient
                    );

                } else {
                    return new OutgoingTransfer(
                            date,
                            amount,
                            description,
                            sender,
                            recipient
                    );
                }
            }catch (TransactionAttributeException e){
                throw new JsonParseException("Amount is negative by a transfer");
            }

        }



    }
}
