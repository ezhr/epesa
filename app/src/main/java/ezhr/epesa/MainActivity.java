package ezhr.epesa;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yarolegovich.slidingrootnav.SlidingRootNav;
import com.yarolegovich.slidingrootnav.SlidingRootNavBuilder;

import net.steamcrafted.materialiconlib.MaterialIconView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import ezhr.epesa.adapters.MainActivityPagerAdapter;
import ezhr.epesa.events.LogoutEvent;
import ezhr.epesa.events.RefreshUserBalanceEvent;
import ezhr.epesa.events.ScannerEvent;
import ezhr.epesa.events.SwitchViewEvent;
import ezhr.epesa.events.TransactionCompleteEvent;
import ezhr.epesa.events.TransactionInfoEvent;
import ezhr.epesa.events.UnlockEvent;

public class MainActivity extends AppCompatActivity implements MainPresenter.MainPresenterListener {


    private SlidingRootNav slidingRootNav;
    private MainActivityPagerAdapter pagerAdapter;

    TextView balanceView;
    @BindView(R.id.mainPagerView)
    MainViewPager pager;
    @BindView(R.id.mainToolbar)
    Toolbar toolbar;
    @BindView(R.id.toolbarTitle)
    TextView toolbarTitle;

    MaterialIconView dashboardIcon;
    MaterialIconView transferIcon;
    MaterialIconView settingsIcon;
    ProgressBar mainBalanceProgressView;

    private Context context = this;
    MainPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!Store.checkToken() || !Store.checkUserName() || !Store.checkUserPhone()) {
            Utils.logOut(this);
            Intent intent = new Intent(this, SigninupActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        presenter = new MainPresenter(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        slidingRootNav = new SlidingRootNavBuilder(this)
                .withToolbarMenuToggle(toolbar)
                .withMenuLayout(R.layout.drawer_activity_main)
                .inject();


        pagerAdapter = new MainActivityPagerAdapter(getSupportFragmentManager());
        pagerAdapter.setContext(this);
        pager.setOffscreenPageLimit(1);
        pager.setAdapter(pagerAdapter);

        // Do not replace these with Butterknife bindings!
        balanceView = findViewById(R.id.mainDrawerBalanceView);
        if (Store.checkBalance()) {
            balanceView.setText(Utils.getFullFormattedCurrency(Store.getBalance()));
        } else {
            balanceView.setText("loading ...");
        }

        dashboardIcon = findViewById(R.id.mainDrawerDashboardIcon);
        transferIcon = findViewById(R.id.mainDrawerTransferIcon);
        settingsIcon = findViewById(R.id.mainDrawerSettingsIcon);
        mainBalanceProgressView = findViewById(R.id.mainBalanceProgressView);

        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == MainActivityPagerAdapter.QRSCANNER_NUMBER) {
                    slidingRootNav.setMenuLocked(true);
                    EventBus.getDefault().post(new ScannerEvent(true));
                } else {
                    slidingRootNav.setMenuLocked(false);
                    EventBus.getDefault().post(new ScannerEvent(false));
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        // Current page
        switchView(0, MainActivityPagerAdapter.DASHBOARD_TEXT, false);

    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onSwitchView(SwitchViewEvent event) {
        switchView(event.pageNumber, event.pageTitle, event.animated);
        //EventBus.getDefault().removeStickyEvent(event);
    }

    @Override
    public void onBackPressed() {
        if (pager.getCurrentItem() == pagerAdapter.QRSCANNER_NUMBER) {
            EventBus.getDefault().post(new SwitchViewEvent(pagerAdapter.TRANSFER_NUMBER, false));
        } else if (!slidingRootNav.isMenuHidden()) {
            slidingRootNav.closeMenu(true);
        } else {
            super.onBackPressed();
        }
    }

    private void switchView(int pageNumber, String pageTitle, boolean animated) {
        Utils.acquirePermissions(this);
        pager.setCurrentItem(pageNumber, animated);
        toolbarTitle.setText(pageTitle);
        dashboardIcon.setColor(ContextCompat.getColor(this, R.color.default_gray));
        transferIcon.setColor(ContextCompat.getColor(this, R.color.default_gray));
        settingsIcon.setColor(ContextCompat.getColor(this, R.color.default_gray));
        switch (pageNumber) {
            case MainActivityPagerAdapter.DASHBOARD_NUMBER:
                dashboardIcon.setColor(ContextCompat.getColor(this, R.color.blue));
                break;
            case MainActivityPagerAdapter.SETTINGS_NUMBER:
                settingsIcon.setColor(ContextCompat.getColor(this, R.color.blue));
                break;
            case MainActivityPagerAdapter.TRANSFER_NUMBER:
                transferIcon.setColor(ContextCompat.getColor(this, R.color.blue));
                break;
            case MainActivityPagerAdapter.QRSCANNER_NUMBER:
                break;
        }
    }

    public void onDrawerItemClick(View view) {
        slidingRootNav.closeMenu(true);
        switch (view.getId()) {
            case (R.id.mainDrawerDashboardView):
                EventBus.getDefault().post(new SwitchViewEvent(pagerAdapter.DASHBOARD_NUMBER, true));
                break;
            case (R.id.mainDrawerTransferView):
                EventBus.getDefault().post(new SwitchViewEvent(pagerAdapter.TRANSFER_NUMBER, true));
                break;
            case (R.id.mainDrawerSettingsView):
                EventBus.getDefault().post(new SwitchViewEvent(pagerAdapter.SETTINGS_NUMBER, true));
                break;
            case (R.id.mainDrawerLogOutView):
                Utils.logOut(this);
                Intent intent = new Intent(this, SigninupActivity.class);
                startActivity(intent);
                finish();
                break;
            default:
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRefreshUserBalanceEvent(RefreshUserBalanceEvent event) {
        presenter.refreshUserBalance(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onScannedEvent(TransactionInfoEvent event) {
        EventBus.getDefault().post(new SwitchViewEvent(pagerAdapter.TRANSFER_NUMBER, true));
    }

    @Override
    protected void onStart() {
        lockApp();
        presenter.refreshUserBalance(this);
        super.onStart();
        if (context == null)
            context = this;
        EventBus.getDefault().register(this);
        slidingRootNav.closeMenu(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        //lockApp();
    }

    @Override
    public void refreshUserBalance(String balance) {
        balanceView.setText(balance);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onTransferSuccessEvent(TransactionCompleteEvent event) {
        if (event.success) {
            presenter.refreshUserBalance(this);
        }
    }

    private void lockApp() {
        UnlockEvent unlockEvent = EventBus.getDefault().removeStickyEvent(UnlockEvent.class);
        if (unlockEvent == null) {
            Intent intent = new Intent(this, LockActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void mainBalanceProgress(boolean loading) {
        if (loading)
            mainBalanceProgressView.setVisibility(View.VISIBLE);
        else
            mainBalanceProgressView.setVisibility(View.GONE);
    }

    @Subscribe (threadMode = ThreadMode.MAIN)
    public void onLogoutEvent(LogoutEvent event) {
        finish();
    }
}
