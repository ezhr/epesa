package ezhr.epesa.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import ezhr.epesa.RegisterIntroFragment;
import ezhr.epesa.RegisterNameFragment;
import ezhr.epesa.RegisterOutroFragment;
import ezhr.epesa.RegisterPasscodeFragment;
import ezhr.epesa.RegisterSettingsFragment;

/**
 * Created by ezhr on 5/10/17.
 */

public class RegisterActivityPagerAdapter extends FragmentPagerAdapter {

    public RegisterActivityPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        switch (position) {
            case 0 :
                fragment = RegisterIntroFragment.getInstance();
                break;
            case 1 :
                fragment = RegisterNameFragment.getInstance();
                break;
            case 2 :
                fragment = RegisterPasscodeFragment.getInstance();
                break;
            case 3 :
                fragment = RegisterSettingsFragment.getInstance();
                break;
            case 4 :
                fragment = RegisterOutroFragment.getInstance();
                break;
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return 5;
    }
}
