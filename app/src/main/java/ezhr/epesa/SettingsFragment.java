package ezhr.epesa;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends Fragment {

    static SettingsFragment fragment;
    Context context;

    public static SettingsFragment newInstance (Context context) {
        if (fragment == null) {
            fragment = new SettingsFragment();
            fragment.context = context;
        }
        return fragment;
    }


    public SettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

}
