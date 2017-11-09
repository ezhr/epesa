package ezhr.epesa;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rengwuxian.materialedittext.MaterialEditText;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import ezhr.epesa.events.RegisterNextButtonEvent;

public class RegisterPasscodeFragment extends Fragment {

    public @BindView(R.id.registerPasscodeEditText1)
    MaterialEditText passcodeText1;
    @BindView(R.id.registerPasscodeEditText2)
    MaterialEditText passcodeText2;

    static RegisterPasscodeFragment fragment;

    public static RegisterPasscodeFragment getInstance() {
        if (fragment == null) {
            fragment = new RegisterPasscodeFragment();
        }
        return fragment;
    }


    public RegisterPasscodeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_register_passcode, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        passcodeText2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (passcodeText1 != null && passcodeText2 != null) {
                    if (passcodeText1.length() < 3) {
                        Utils.shakeView(passcodeText1);
                    } else {
                        if (passcodeText1.length() == passcodeText2.length()) {
                            if (!passcodeText1.getText().toString().equals(passcodeText2.getText().toString())) {
                                Utils.shakeView(passcodeText1);
                                Utils.shakeView(passcodeText2);
                                Utils.displayWarning(getActivity(), "The passcodes entered do not match!");
                                passcodeText2.setText("");
                            } else {
                                EventBus.getDefault().post(new RegisterNextButtonEvent(true));
                                return;
                            }
                        }
                        if (passcodeText2.length() > passcodeText1.length()) {
                            Utils.shakeView(passcodeText2);
                            passcodeText2.setText("");
                        }
                    }
                    EventBus.getDefault().post(new RegisterNextButtonEvent(false));
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }
}
