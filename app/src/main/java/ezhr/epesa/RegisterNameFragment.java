package ezhr.epesa;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blankj.utilcode.util.KeyboardUtils;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import ezhr.epesa.events.RegisterNextButtonEvent;
import retrofit2.http.Path;

public class RegisterNameFragment extends Fragment {

    @BindView(R.id.registerNameEditText)
    MaterialEditText nameEditText;

    private static RegisterNameFragment fragment;

    public static RegisterNameFragment getInstance() {
        if (fragment == null) {
            fragment = new RegisterNameFragment();
        }
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_register_name, container, false);
        ButterKnife.bind(this, view);
        nameEditText.setText("");
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        nameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence != null) {
                    if (charSequence.length() > 3) {
                        EventBus.getDefault().post(new RegisterNextButtonEvent(true));
                    } else {
                        EventBus.getDefault().post(new RegisterNextButtonEvent(false));
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }
}
