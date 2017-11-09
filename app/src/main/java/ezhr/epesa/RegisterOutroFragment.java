package ezhr.epesa;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 */
public class RegisterOutroFragment extends Fragment {

    @BindView(R.id.registerOutroProgressView)
    ProgressBar progressView;
    @BindView(R.id.registerOutroNameText)
    TextView nameText;
    @BindView(R.id.registerOutroSuccessText)
    TextView successText;

    static RegisterOutroFragment fragment;

    public static RegisterOutroFragment getInstance() {
        if (fragment == null)
            fragment = new RegisterOutroFragment();
        return fragment;
    }


    public RegisterOutroFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_register_outro, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Utils.setProgressColor(getActivity(), progressView, R.color.white);
    }
}
