package ezhr.epesa;

import com.facebook.stetho.inspector.protocol.module.Console;
import com.orhanobut.hawk.Hawk;
import com.orhanobut.logger.Logger;

import org.json.JSONObject;

import java.util.List;

import ezhr.epesa.models.Transaction;

/**
 * Created by ezhr on 7/9/17.
 */

public class Store {

    private static final String TOKEN_KEY = "com.ezhr.epesa.TOKEN_FILE.TOKEN_KEY";

    private static final String USER_NAME_KEY = "com.ezhr.epesa.USER_FILE.USER_NAME_KEY";
    private static final String USER_PHONE_KEY = "com.ezhr.epesa.USER_FILE.USER_PHONE_KEY";

    private static final String PASSCODE_KEY = "com.ezhr.epesa.PASSCODE_KEY";
    private static final String PASSCODE_LENGTH = "com.ezhr.epesa.PASSCODE_LENGTH";

    private static final String BALANCE_KEY = "com.ezhr.epesa.BALANCE_KEY";

    private static final String TRANSACTION_NUMBER_KEY = "com.ezhr.epesa.TRANSACTION_NUMBER_KEY";
    private static final String TRANSACTION_AMOUNT_KEY = "com.ezhr.epesa.TRANSACTION_AMOUNT_KEY";
    private static final String TRANSACTION_MESSAGE_KEY = "com.ezhr.epesa.TRANSACTION_MESSAGE_KEY";

    private static final String COUNTRY_CODE_KEY = "com.ezhr.epesa.COUNTRY_CODE_KEY";

    // Clear All Values:
    public static void clearAll() {
        Hawk.deleteAll();
    }

    // Token Methods:
    public static void saveToken(String token) {
        Hawk.put(TOKEN_KEY, token);
    }

    public static String getToken() {
        return Hawk.get(TOKEN_KEY, "");
    }

    public static boolean checkToken() {
        return Hawk.contains(TOKEN_KEY);
    }

    // Name Methods:
    public static void saveUserName(String name) {
        Hawk.put(USER_NAME_KEY, name);
    }

    public static String getUserName() {
        return Hawk.get(USER_NAME_KEY, null);
    }

    public static boolean checkUserName() {
        return Hawk.contains(USER_NAME_KEY);
    }

    public static void saveUserPhone(String phone) {
        Hawk.put(USER_PHONE_KEY, phone);
    }

    public static String getUserPhone() {
        return Hawk.get(USER_PHONE_KEY, "");
    }

    public static boolean checkUserPhone() {
        return Hawk.contains(USER_PHONE_KEY);
    }

    // Passcode Methods:
    public static void savePasscode(String passcode) {
        Hawk.put(PASSCODE_KEY, passcode);
    }

    public static String getPasscode() {
        return Hawk.get(PASSCODE_KEY, "111111");
    }

    public static boolean checkPasscode() {
        return Hawk.contains(PASSCODE_KEY);
    }

    public static void savePasscodeLength(int length) {
        Hawk.put(PASSCODE_LENGTH, length);
    }

    public static int getPasscodeLength() {
        return Hawk.get(PASSCODE_LENGTH, 0);
    }

    // Balance Methods:
    public static void saveBalance(float balance) {
        Hawk.put(BALANCE_KEY, balance);
    }

    public static float getBalance() {
        return Hawk.get(BALANCE_KEY, 0f);
    }

    public static boolean checkBalance() {
        return Hawk.contains(BALANCE_KEY);
    }

    // Transaction Methods:
    public static boolean checkTransaction () {
        return (Hawk.contains(TRANSACTION_NUMBER_KEY) && Hawk.contains(TRANSACTION_AMOUNT_KEY));
    }

    public static void saveRawTransaction (String number, String amount, String message) {
        Hawk.put(TRANSACTION_NUMBER_KEY, number);
        Hawk.put(TRANSACTION_AMOUNT_KEY, amount);
        Hawk.put(TRANSACTION_MESSAGE_KEY, message);
    }

    public static Transaction getTransaction () {
        return Utils.parseTransaction(
                Hawk.get(TRANSACTION_NUMBER_KEY, ""),
                Hawk.get(TRANSACTION_AMOUNT_KEY, ""),
                Hawk.get(TRANSACTION_MESSAGE_KEY, "")
        );
    }

    public static JSONObject getRawTransaction () {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("number", Hawk.get(TRANSACTION_NUMBER_KEY, ""));
            jsonObject.put("amount", Hawk.get(TRANSACTION_AMOUNT_KEY, ""));
            jsonObject.put("message", Hawk.get(TRANSACTION_MESSAGE_KEY, ""));
        } catch (Exception e) {
            Logger.e(e.toString());
        }
        return jsonObject;
    }

    public static void clearTransaction () {
        Hawk.delete(TRANSACTION_NUMBER_KEY);
        Hawk.delete(TRANSACTION_AMOUNT_KEY);
        Hawk.delete(TRANSACTION_MESSAGE_KEY);
    }

    // Other Methods:
    public static void saveCountryCode (String countryCode) { Hawk.put(countryCode, COUNTRY_CODE_KEY);}

    public static String getCountryCode () { return String.valueOf(Hawk.get(COUNTRY_CODE_KEY)); }

}
