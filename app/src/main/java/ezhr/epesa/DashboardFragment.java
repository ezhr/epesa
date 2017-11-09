package ezhr.epesa;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mostafaaryan.transitionalimageview.TransitionalImageView;
import com.mostafaaryan.transitionalimageview.model.TransitionalImage;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import ezhr.epesa.adapters.TransactionsAdapter;
import ezhr.epesa.events.FetchTransactionsEvent;
import ezhr.epesa.events.TransactionCompleteEvent;


public class DashboardFragment extends Fragment implements DashboardPresenter.DashboardPresenterListener {

    @BindView(R.id.dashboardCardNameView)
    TextView cardName;
    @BindView(R.id.dashboardCardPhoneNumberView)
    TextView cardNumber;
    @BindView(R.id.dashboardCardQrImageView)
    TransitionalImageView cardQrImage;
    @BindView(R.id.dashboardTransactionsRecyclerView)
    RecyclerView transactionsRecyclerView;
    @BindView(R.id.activityProgressView)
    ProgressBar activityProgressView;
    @BindView(R.id.activitySwipeLayout)
    SwipeRefreshLayout activitySwipeLayout;

    TransactionsAdapter transactionsAdapter;

    DashboardPresenter presenter;

    private Context context;

    public DashboardFragment() {
        // Required empty public constructor
    }

    public static DashboardFragment newInstance(Context context) {
        DashboardFragment fragment = new DashboardFragment();
        fragment.setContext(context);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = new DashboardPresenter(this, getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        presenter.getUserInfo();
        presenter.getUserQr(getActivity());
        transactionsRecyclerView.setLayoutManager(new LinearLayoutManager(context));

        transactionsAdapter = new TransactionsAdapter(R.layout.transactions_single_view, presenter.getTransactionsData());
        transactionsAdapter.setContext(context);
        transactionsRecyclerView.setAdapter(transactionsAdapter);

        activitySwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                activitySwipeLayout.setRefreshing(false);
                presenter.getTransactions();
            }
        });

        presenter.getTransactions();

    }

    @Subscribe(threadMode =  ThreadMode.MAIN)
    public void onTransactionCompleteEvent (TransactionCompleteEvent event) {
        if (event.success) {
            //presenter.fetchTransactionsFromDatabase(presenter.getTransactionsData().get(0).getDate());
            presenter.getTransactions();
        }
    }

    private void setContext(Context context) {
        this.context = context;
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFetchTransactionsEvent(FetchTransactionsEvent event) {
        //presenter.fetchTransactions(true);
    }

    @Override
    public void configureUserCardInfo(String phoneString, String name) {
        cardNumber.setText(phoneString);
        cardName.setText(name);
    }

    @Override
    public void configureUserCardQr(Bitmap userQr) {
        final TransitionalImage qrImage = new TransitionalImage.Builder()
                .image(userQr)
                .create();
        cardQrImage.setTransitionalImage(qrImage);

        /*cardQrImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Logger.d("CLICKED");
                EventBus.getDefault().postSticky(new UnlockEvent());
            }
        });*/
        cardQrImage.setVisibility(View.VISIBLE);
    }

    @Override
    public void refreshTransactions() {
        transactionsAdapter.notifyDataSetChanged();
    }

    @Override
    public void activityProgress(boolean loading) {
        Utils.setProgressColor(context, activityProgressView, R.color.white);
        if (loading)
            activityProgressView.setVisibility(View.VISIBLE);
        else
            activityProgressView.setVisibility(View.GONE);
    }
}
