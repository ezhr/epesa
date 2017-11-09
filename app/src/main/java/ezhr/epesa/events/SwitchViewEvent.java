package ezhr.epesa.events;

import android.graphics.Color;

import ezhr.epesa.MainActivity;
import ezhr.epesa.adapters.MainActivityPagerAdapter;

/**
 * Created by ezhr on 23/9/17.
 */

public class SwitchViewEvent {
    public int pageNumber;
    public String pageTitle;
    public boolean animated;

    public SwitchViewEvent(int pageNumber, boolean animated) {
        this.pageNumber = pageNumber;
        this.animated = animated;
        pageTitle = MainActivityPagerAdapter.getTitleText(pageNumber);
    }
}
