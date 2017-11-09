package ezhr.epesa.events;

/**
 * Created by ezhr on 22/9/17.
 */

public class FetchTransactionsEvent {

    boolean refresh;

    public FetchTransactionsEvent(boolean refresh) {
        this.refresh = refresh;
    }
}
