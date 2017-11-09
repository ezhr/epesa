package ezhr.epesa.models;

/**
 * Created by ezhr on 6/9/17.
 */

public class StandardResponses {

    private String message;
    private String token;
    private User user;
    private int passcodeLength;


    public String getToken() {
        return token;
    }

    public User getUser() {
        return user;
    }

    public String getMessage() {
        return message;
    }

    public int getPasscodeLength() { return passcodeLength; }

}
