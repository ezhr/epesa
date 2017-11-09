package ezhr.epesa.events;

/**
 * Created by ezhr on 27/9/17.
 */

public class TransactionCompleteEvent {

    public boolean success;
    public String message;
    //Todo get after amount to put into confirmation message

    public TransactionCompleteEvent(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
