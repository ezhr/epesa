package ezhr.epesa.events;

import ezhr.epesa.models.Transaction;

/**
 * Created by ezhr on 27/9/17.
 */

public class StartTransferServiceEvent {
    public String passcode;

    public StartTransferServiceEvent(String passcode) {
        this.passcode = passcode;
    }

}
