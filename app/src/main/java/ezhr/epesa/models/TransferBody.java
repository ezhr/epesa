package ezhr.epesa.models;

/**
 * Created by ezhr on 27/9/17.
 */

public class TransferBody {

    float amount;
    String message;

    public TransferBody(float amount, String message) {
        this.amount = amount;
        this.message = message;
    }
}
