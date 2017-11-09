package ezhr.epesa;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;

import com.blankj.utilcode.util.KeyboardUtils;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import ezhr.epesa.events.LogoutEvent;
import ezhr.epesa.events.UnlockEvent;
import ezhr.epesa.models.StandardResponses;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.view.View.GONE;

public class LockActivity extends AppCompatActivity {

    @BindView(R.id.passcodeView)
    ezhr.epesa.PasscodeView passcodeView;
    @BindView(R.id.lockActivityLoadingView)
    LinearLayout loadingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock);

        ButterKnife.bind(this);

        Utils.acquirePermissions(this);

        //Store.clearToken(context);
        //Store.clearUser(context);

        passcodeView.setPasscodeEntryListener(new ezhr.epesa.PasscodeView.PasscodeEntryListener() {
            @Override
            public void onPasscodeEntered(final String passcode) {
                // Todo remove this!!!!
                if (passcode.equals("000000")) {
                    Utils.logOut(LockActivity.this);
                    finishAffinity();
                    return;
                }

                if (Store.checkPasscode()) {
                    if (passcode.equals(Store.getPasscode())) {
                        KeyboardUtils.hideSoftInput(LockActivity.this);
                        EventBus.getDefault().postSticky(new UnlockEvent());
                        finish();
                    } else {
                        passcodeView.clearText();
                        Utils.shakeView(passcodeView);
                    }
                } else {
                    loadingView.setVisibility(View.VISIBLE);
                    KeyboardUtils.hideSoftInput(LockActivity.this);
                    Call<StandardResponses> call = Utils.getApi().checkPasscode(Store.getToken(), passcode);
                    call.enqueue(new Callback<StandardResponses>() {
                        @Override
                        public void onResponse(Call<StandardResponses> call, Response<StandardResponses> response) {
                            if (response.code() == 200) {
                                Store.savePasscode(passcode);
                                EventBus.getDefault().postSticky(new UnlockEvent());
                                finish();
                            } else {
                                Utils.displayWarning(LockActivity.this, response.errorBody());
                                passcodeView.clearText();
                                Utils.shakeView(passcodeView);
                                passcodeView.requestToShowKeyboard();
                                loadingView.setVisibility(GONE);
                            }
                            loadingView.setVisibility(GONE);
                        }

                        @Override
                        public void onFailure(Call<StandardResponses> call, Throwable t) {
                            Logger.d(t.toString());
                            Utils.displayError(LockActivity.this, t);
                            loadingView.setVisibility(GONE);
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }

    @Subscribe (threadMode = ThreadMode.MAIN)
    public void onLogoutEvent(LogoutEvent event) {
        Utils.logOut(this);
        Intent intent = new Intent(this, SigninupActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
}
