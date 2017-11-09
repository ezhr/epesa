package ezhr.epesa.events;

import ezhr.epesa.models.Transaction;

/**
 * Created by ezhr on 12/9/17.
 */

public class TransactionInfoEvent {

    public Transaction transactionInfo;

    public TransactionInfoEvent(Transaction transactionInfo) {
        this.transactionInfo = transactionInfo;
    }
}
