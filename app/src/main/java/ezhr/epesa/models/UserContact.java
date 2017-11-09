package ezhr.epesa.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by ezhr on 11/9/17.
 */

public class UserContact extends RealmObject {

    private String name;

    @PrimaryKey
    private String phone;

    public UserContact() {

    }

    public UserContact(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

}
