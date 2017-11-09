package ezhr.epesa.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by ezhr on 12/9/17.
 */

public class Transaction extends RealmObject {

    @PrimaryKey
    public String _id;

    public float amount;
    public String contactPhone;
    public String contactName;
    public long date;
    public String message;
    public Boolean sent;

    public Transaction() {

    }

    public Transaction(String contactName, String contactPhone, float amount, String message) {
        this.contactName = contactName;
        this.contactPhone = contactPhone;
        this.message = message;
        this.amount = amount;
    }

    public Transaction(String contactName, String contactPhone) {
        this.contactName = contactName;
        this.contactPhone = contactPhone;
    }

    public void clear () {
        amount = 0;
        contactName = "";
        contactPhone = "";
        message = "";
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Float getAmount() {
        return amount;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public String getContactName() {
        return contactName;
    }

    public Long getDate() {
        return date;
    }

    public String getMessage() {
        return message;
    }

    public Boolean wasSent() {
        return sent;
    }

    public String getId() {
        return _id;
    }
}
