package ezhr.epesa.models;

/**
 * Created by ezhr on 9/9/17.
 */

public class User {

    private String _id;
    private String phone;
    private float balance;
    private String name;
    private String passcode;
    private String countryCode;

    public User(String name, String passcode, String countryCode) {
        this.name = name;
        this.passcode = passcode;
        this.countryCode = countryCode;
    }

    public String getId() {
        return _id;
    }

    public String getPhone() {
        return phone;
    }

    public Float getBalance() {
        return balance;
    }

    public String getName() { return name; }

}
