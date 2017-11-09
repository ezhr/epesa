package ezhr.epesa;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 */
public class RegisterSettingsFragment extends Fragment {

    static RegisterSettingsFragment fragment;

    public static RegisterSettingsFragment getInstance() {
        if (fragment == null)
            fragment = new RegisterSettingsFragment();
        return fragment;
    }


    public RegisterSettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register_settings, container, false);
    }

}
