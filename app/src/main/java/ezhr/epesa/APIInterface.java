package ezhr.epesa;

import android.text.style.AlignmentSpan;

import java.util.List;

import ezhr.epesa.models.Transaction;
import ezhr.epesa.models.TransferBody;
import ezhr.epesa.models.User;
import ezhr.epesa.models.StandardResponses;
import ezhr.epesa.models.UserContact;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Created by ezhr on 6/9/17.
 */

public interface APIInterface {

    // TODO IOException means network failed. Implement proper error handling for this + all other request errors in all calls


    @GET("user/finduser")
    Call<StandardResponses> findUser (@Header("xx-phone") String phone);

    @POST("user/new")
    Call<StandardResponses> newUser (@Header("xx-phone") String phone, @Header("xx-devicetoken") String deviceToken, @Body User user);

    //@GET("user/info")
    //Call<User> getUserInfo (@Header("xx-phone") String phone);

    @GET("user/signin")
    Call<StandardResponses> signIn (@Header("xx-phone") String phone);

    @GET("user/refreshdevicetoken")
    Call<StandardResponses> refreshDeviceToken (@Header("xx-token") String token, @Header("xx-devicetoken") String deviceToken);

    @GET("user/checkpasscode")
    Call<StandardResponses> checkPasscode (@Header("xx-token") String token, @Header("xx-passcode") String passcode);

    @GET("user/balance")
    Call<User> getUserBalance (@Header("xx-token") String token);

    @GET("user/transactions/{from}")
    Call<List<Transaction>> getUserTransactions (@Header("xx-token") String token, @Path("from") long from);

    @POST("user/contacts/check")
    Call<List<UserContact>> checkContacts (@Header("xx-token") String token, @Body List<UserContact> contacts);

    @POST("user/transfer/{phone}")
    Call<StandardResponses> transfer (@Header("xx-token") String token, @Header("xx-passcode") String passcode, @Path("phone") String
            phone, @Body TransferBody transferBody);
}
