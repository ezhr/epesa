package ezhr.epesa.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import ezhr.epesa.DashboardFragment;
import ezhr.epesa.QrScannerFragment;
import ezhr.epesa.SettingsFragment;
import ezhr.epesa.TransferFragment;

/**
 * Created by ezhr on 21/9/17.
 */

public class MainActivityPagerAdapter extends FragmentPagerAdapter {

    private Context context;
    public static final int DASHBOARD_NUMBER = 0;
    public static final String DASHBOARD_TEXT = "DASHBOARD";
    public static final int TRANSFER_NUMBER = 1;
    public static final String TRANSFER_TEXT = "TRANSFER";
    public static final int QRSCANNER_NUMBER = 3;
    public static final String QRSCANNER_TEXT = "SCANNER";
    public static final int SETTINGS_NUMBER = 2;
    public static final String SETTINGS_TEXT = "SETTINGS (COMING SOON)";

    public MainActivityPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case DASHBOARD_NUMBER :
                return DashboardFragment.newInstance(context);
            case TRANSFER_NUMBER:
                return TransferFragment.newInstance(context);
            case QRSCANNER_NUMBER:
                return QrScannerFragment.newInstance(context);
            case SETTINGS_NUMBER:
                return SettingsFragment.newInstance(context);
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 4;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public static String getTitleText (int position) {
        switch (position) {
            case DASHBOARD_NUMBER :
                return DASHBOARD_TEXT;
            case TRANSFER_NUMBER:
                return TRANSFER_TEXT;
            case QRSCANNER_NUMBER:
                return QRSCANNER_TEXT;
            case SETTINGS_NUMBER:
                return SETTINGS_TEXT;
            default:
                return null;
        }
    }
}
