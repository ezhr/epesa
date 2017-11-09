package ezhr.epesa;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import ezhr.epesa.events.LogoutEvent;
import ezhr.epesa.models.User;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by ezhr on 25/9/17.
 */

public class MainPresenter {

    static MainPresenterListener listener;

    public MainPresenter(Activity activity) {
        this.listener = (MainPresenterListener) activity;
    }

    public void refreshUserBalance(final Context context) {

        listener.mainBalanceProgress(true);

        // todo change this
        Call<User> call = Utils.getApi().getUserBalance(Store.getToken());
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.code() == 200) {
                    float balance = response.body().getBalance();
                    String balanceString = Utils.getFullFormattedCurrency(response.body().getBalance());
                    listener.refreshUserBalance(balanceString);
                    Store.saveBalance(balance);
                } else {
                    if (response.code() == 400) {
                        EventBus.getDefault().post(new LogoutEvent());
                    }
                    Utils.displayWarning(context, response.errorBody());
                }
                listener.mainBalanceProgress(false);
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e(Utils.getTag(), "onFailure: " + t.toString());
                Utils.displayError(context, t);
                listener.mainBalanceProgress(false);
            }
        });
    }

    public interface MainPresenterListener {
        void refreshUserBalance(String balance);

        void mainBalanceProgress(boolean loading);
    }

}
