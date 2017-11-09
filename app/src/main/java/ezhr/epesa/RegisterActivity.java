package ezhr.epesa;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.appolica.flubber.Flubber;
import com.appolica.flubber.listener.SimpleAnimatorListener;
import com.blankj.utilcode.util.KeyboardUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.github.clans.fab.FloatingActionButton;
import com.google.firebase.iid.FirebaseInstanceId;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;
import ezhr.epesa.adapters.RegisterActivityPagerAdapter;
import ezhr.epesa.events.RegisterNextButtonEvent;
import ezhr.epesa.events.UnlockEvent;
import ezhr.epesa.models.StandardResponses;
import ezhr.epesa.models.User;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    @BindView(R.id.registerViewPager)
    MainViewPager pager;
    @BindView(R.id.registerNextButton)
    FloatingActionButton nextButton;

    RegisterActivityPagerAdapter adapter;

    User user;
    String phone;
    String countryCode;
    String name;
    String passcode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ButterKnife.bind(this);

        Intent intent = getIntent();
        phone = intent.getStringExtra(SigninupActivity.PHONE_INTENT_STRING);
        countryCode = intent.getStringExtra(SigninupActivity.PHONE_COUNTRYCODE_STRING);

        Utils.acquirePermissions(this);

        adapter = new RegisterActivityPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);
        pager.setPageMargin(SizeUtils.dp2px(10));

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pager.getCurrentItem() == adapter.getCount() - 1) {
                    Intent mainActivityIntent = new Intent(RegisterActivity.this, MainActivity.class);
                    startActivity(mainActivityIntent);
                    finish();
                } else {
                    pager.setCurrentItem(pager.getCurrentItem() + 1, true);
                }
            }
        });

        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 1 || position == 2) {
                    KeyboardUtils.showSoftInput(RegisterActivity.this);
                    hideNextButton();
                } else {
                    KeyboardUtils.hideSoftInput(pager);
                }
                if (position == 2) {
                    String name = RegisterNameFragment.getInstance().nameEditText.getText().toString();
                    //RegisterOutroFragment.getInstance().nameText.setText("all set, " + name);
                }
                if (position == adapter.getCount() - 1) {
                    hideNextButton();
                }
                if (position == adapter.getCount() - 1) {
                    register();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNextButtonEvent(RegisterNextButtonEvent event) {
        if (event.show) {
            showNextButton();
        } else {
            hideNextButton();
        }
    }

    private void showNextButton() {
        if (nextButton.getVisibility() == View.INVISIBLE) {
            nextButton.setVisibility(View.VISIBLE);
            Flubber.with()
                    .autoStart(true)
                    .duration(500)
                    .velocity(0.45f)
                    .animation(Flubber.AnimationPreset.SLIDE_UP)
                    .createFor(nextButton);
        }
    }

    private void hideNextButton() {
        if (nextButton.getVisibility() == View.VISIBLE) {
            Flubber.with()
                    .autoStart(true)
                    .duration(150)
                    .animation(Flubber.AnimationPreset.TRANSLATION_Y)
                    .translateY(300)
                    .createFor(nextButton)
                    .addListener(new SimpleAnimatorListener() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            nextButton.setVisibility(View.INVISIBLE);
                        }
                    });

        }
    }

    private void register() {
        name = RegisterNameFragment.getInstance().nameEditText.getText().toString();
        name = name.trim();
        passcode = RegisterPasscodeFragment.getInstance().passcodeText1.getText().toString();
        user = new User(name, passcode, countryCode);
        String deviceToken = FirebaseInstanceId.getInstance().getToken();
        Call<StandardResponses> call = Utils.getApi().newUser(phone, deviceToken, user);
        call.enqueue(new Callback<StandardResponses>() {
            @Override
            public void onResponse(Call<StandardResponses> call, Response<StandardResponses> response) {
                if (response.code() == 200) {
                    Store.saveUserName(name);
                    Store.saveUserPhone(phone);
                    Store.savePasscode(passcode);
                    final String successMessage = response.body().getMessage();
                    Call<StandardResponses> signInTokenCall = Utils.getApi().signIn(phone);
                    signInTokenCall.enqueue(new Callback<StandardResponses>() {
                        @Override
                        public void onResponse(Call<StandardResponses> call, Response<StandardResponses> tokenResponse) {
                            if (tokenResponse.code() == 200) {
                                Store.saveToken(tokenResponse.body().getToken());
                                EventBus.getDefault().postSticky(new UnlockEvent());
                                RegisterOutroFragment fragment = RegisterOutroFragment.getInstance();
                                fragment.nameText.setText("all set.");
                                fragment.progressView.setVisibility(View.GONE);
                                fragment.successText.setText(successMessage);
                                fragment.successText.setVisibility(View.VISIBLE);
                                showNextButton();
                            } else {
                                Utils.displayWarning(RegisterActivity.this, tokenResponse.errorBody());
                            }
                        }
                        @Override
                        public void onFailure(Call<StandardResponses> call, Throwable t) {
                            Utils.displayError(RegisterActivity.this, t);
                        }
                    });
                } else {
                    Utils.displayWarning(RegisterActivity.this, response.errorBody());
                }
            }
            @Override
            public void onFailure(Call<StandardResponses> call, Throwable t) {
                Utils.displayError(RegisterActivity.this, t);
            }
        });
    }
}
