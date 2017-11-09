package ezhr.epesa;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.blankj.utilcode.util.KeyboardUtils;
import com.hbb20.CountryCodePicker;
import com.orhanobut.logger.Logger;
import com.rengwuxian.materialedittext.MaterialEditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;
import ezhr.epesa.models.StandardResponses;
import mehdi.sakout.fancybuttons.FancyButton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SigninupActivity extends AppCompatActivity {

    public static String PHONE_INTENT_STRING = "phone";
    public static String PHONE_COUNTRYCODE_STRING = "countrycode";

    @BindView(R.id.signinupPhoneInput)
    MaterialEditText phoneInput;
    @BindView(R.id.signinupPhoneCountry)
    CountryCodePicker countryPicker;
    @BindView(R.id.signInButton)
    FancyButton signInButton;
    @BindView(R.id.signUpButton)
    FancyButton signUpButton;
    @BindView(R.id.signinupProgress)
    ProgressBar progress;
    @BindView(R.id.signinupPhoneInputView)
    LinearLayout phoneInputView;

    final Context context = SigninupActivity.this;
    String phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signinup);

        ButterKnife.bind(this);

        Utils.acquirePermissions(this);

        countryPicker.registerCarrierNumberEditText(phoneInput);
        //countryPicker.selectCountryFromNetworkAndSimInfo();

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInUp(true);
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInUp(false);
            }
        });

        countryPicker.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected() {
                if (countryPicker.getSelectedCountryCodeAsInt() != 254) {
                    Utils.shakeView(countryPicker);
                    Toasty.error(SigninupActivity.this, "Unfortunately, epesa is only supported in Tanzania for the time being, but we're" +
                            " working hard to launch in your region soon.  Stay tuned for updates!", Toast.LENGTH_LONG).show();
                }
            }
        });

        phoneInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable != null && editable.length() > 0) {
                    String s = editable.toString();
                    if (s.length() == 1 && s.equals("0")) {
                        phoneInput.setText("");
                    }
                }
            }
        });


    }

    private void signInUp(boolean signin) {
        final String phoneInputText = countryPicker.getFullNumber();
        if (phoneInputText.length() < 12) {
            Utils.shakeView(phoneInputView);
            return;
        }
        if (countryPicker.getSelectedCountryCodeAsInt() != 255) {
            Utils.shakeView(countryPicker);
            return;
        }
        setLoading(true);
        KeyboardUtils.hideSoftInput(this);

        if (phoneInput.getText().toString().startsWith("0")) {
            phone = countryPicker.getSelectedCountryCode() + phoneInputText.substring(1);
        } else {
            phone = phoneInputText;
        }
        if (signin) {
            Call<StandardResponses> call = Utils.getApi().signIn(phone);
            call.enqueue(new Callback<StandardResponses>() {
                @Override
                public void onResponse(Call<StandardResponses> call, Response<StandardResponses> response) {
                    if (response.code() == 200) {
                        Store.saveCountryCode(countryPicker.getSelectedCountryCode());
                        Store.saveToken(response.body().getToken());
                        Store.saveUserPhone(response.body().getUser().getPhone());
                        Store.saveUserName(response.body().getUser().getName());
                        Store.savePasscodeLength(response.body().getPasscodeLength());
                        Intent intent = new Intent(SigninupActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Utils.shakeView(phoneInputView);
                        Utils.displayWarning(SigninupActivity.this, response.errorBody());
                    }
                    setLoading(false);
                }

                @Override
                public void onFailure(Call<StandardResponses> call, Throwable t) {
                    Logger.d(t.toString());
                    Utils.displayError(SigninupActivity.this, t);
                    setLoading(false);
                }
            });
        } else {
            Call<StandardResponses> call = Utils.getApi().findUser(phone);
            call.enqueue(new Callback<StandardResponses>() {
                @Override
                public void onResponse(Call<StandardResponses> call, Response<StandardResponses> response) {
                    if (response.code() == 200) {
                        Store.saveCountryCode(countryPicker.getSelectedCountryCode());
                        Intent intent = new Intent(SigninupActivity.this, RegisterActivity.class);
                        intent.putExtra(PHONE_INTENT_STRING, phoneInputText);
                        intent.putExtra(PHONE_COUNTRYCODE_STRING, countryPicker.getSelectedCountryCode());
                        startActivity(intent);
                        finish();
                    } else {
                        Utils.shakeView(phoneInputView);
                        Utils.displayWarning(SigninupActivity.this, response.errorBody());
                    }
                    setLoading(false);
                }

                @Override
                public void onFailure(Call<StandardResponses> call, Throwable t) {
                    Logger.d(t.toString());
                    Utils.displayError(SigninupActivity.this, t);
                    setLoading(false);
                }
            });
        }
    }

    private void setLoading(boolean start) {
        if (start) {
            countryPicker.setCcpClickable(false);
            phoneInput.setEnabled(false);
            progress.setVisibility(View.VISIBLE);
            signInButton.setEnabled(false);
            signUpButton.setEnabled(false);
        } else {
            countryPicker.setCcpClickable(true);
            phoneInput.setEnabled(true);
            progress.setVisibility(View.GONE);
            signInButton.setEnabled(true);
            signUpButton.setEnabled(true);
        }
    }
}
