package ezhr.epesa;

import android.text.style.AlignmentSpan;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.orhanobut.logger.Logger;

import java.io.IOException;

import ezhr.epesa.models.StandardResponses;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class EpesaFirebaseInstanceIDService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        if (Store.checkToken()) {
            Call<StandardResponses> call = Utils.getApi().refreshDeviceToken(Store.getToken(), FirebaseInstanceId.getInstance().getToken());
            call.enqueue(new Callback<StandardResponses>() {
                @Override
                public void onResponse(Call<StandardResponses> call, Response<StandardResponses> response) {
                    if (response.code() != 200) {
                        try {
                            Logger.e("Failed to refresh device token! " + response.errorBody().string());
                        } catch (IOException e) {
                            Logger.e(e.toString());
                        }
                    }
                }

                @Override
                public void onFailure(Call<StandardResponses> call, Throwable t) {
                    Logger.e(t.toString());
                }
            });
        }
    }
}
