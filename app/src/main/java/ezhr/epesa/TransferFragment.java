package ezhr.epesa;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.GridLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.blankj.utilcode.util.KeyboardUtils;
import com.blankj.utilcode.util.ServiceUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.orhanobut.logger.Logger;
import com.rengwuxian.materialedittext.MaterialEditText;

import net.steamcrafted.materialiconlib.MaterialIconView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTouch;
import ezhr.epesa.adapters.ContactsAdapter;
import ezhr.epesa.adapters.MainActivityPagerAdapter;
import ezhr.epesa.events.StartTransferServiceEvent;
import ezhr.epesa.events.SwitchViewEvent;
import ezhr.epesa.events.TransactionCompleteEvent;
import ezhr.epesa.events.TransactionInfoEvent;
import ezhr.epesa.models.Transaction;
import mehdi.sakout.fancybuttons.FancyButton;

import static android.view.View.GONE;

public class TransferFragment extends Fragment implements TransferPresenter.TransferPresenterListener {

    String message = "";
    float fee;

    @BindView(R.id.transferParentLayout)
    ConstraintLayout layout;

    @OnTouch(R.id.transferParentLayout)
    public boolean parentLayoutTouch(View view) {
        view.requestFocus();
        if (contactsCard.getVisibility() == View.VISIBLE) {
            hideContacts();
        }
        KeyboardUtils.hideSoftInput(getActivity());
        return true;
    }

    @BindView(R.id.numberCardView)
    CardView numberCardView;

    @BindView(R.id.transferNumberEditText)
    MaterialEditText numberEditText;

    @BindView(R.id.refreshContactsProgressView)
    ProgressBar refreshContactsProgressView;

    @BindView(R.id.clearTextButton)
    MaterialIconView clearTextButton;

    @OnClick(R.id.transferStartQRButton)
    public void startQRButtonClick() {
        loadQrScanner();
    }

    @OnClick(R.id.transferContactsEditTextOverflow)
    public void contactsEditTextOverflow(View view) {
        PopupMenu popup = new PopupMenu(context, view);
        popup.inflate(R.menu.transfer_contacts_overflow);
        popup.show();
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                presenter.refreshContactsDatabase();
                return true;
            }
        });
    }

    @BindView(R.id.transferContactsCard)
    CardView contactsCard;
    @BindView(R.id.transferContactsRecyclerView)
    RecyclerView contactsRecyclerView;

    @BindView(R.id.transferMessageEditText)
    MaterialEditText messageEditText;

    @BindView(R.id.transferAmountLayout)
    GridLayout amountLayout;
    @BindView(R.id.transferAmountEditText)
    MaterialEditText amountEditText;

    @BindView(R.id.transferFeeTextView)
    TextView feeTextView;
    @BindView(R.id.transferTotalTextView)
    TextView totalTextView;

    @BindView(R.id.transferSendMoneyButton)
    FancyButton sendMoneyButton;

    @BindView(R.id.transferProgressView)
    ProgressBar transferProgressView;

    @OnClick(R.id.transferSendMoneyButton)
    public void sendMoneyButton(View view) {
        if (numberEditText.getText().length() < 1 || amountEditText.getText().length() < 1) {
            if (numberEditText.getText().length() < 1) {
                Utils.shakeView(numberCardView);
            }
            if (amountEditText.getText().length() < 1) {
                Utils.shakeView(amountLayout);
            }
        } else {

            if (messageEditText.length() > 0) {
                message = messageEditText.getText().toString();
            }
            final String rawNumberText = numberEditText.getText().toString();
            final String amountString = amountEditText.getText().toString();

            Transaction transactionInfo = Utils.parseTransaction(rawNumberText, amountString, message);
            if (transactionInfo == null) {
                Utils.displayWarning(context, "The number entered is invalid, please check and try again!");
                return;
            }
            presenter.setTransactionInfo(transactionInfo);

            String confirmString = "To: " + rawNumberText;
            confirmString += "\n\nAmount: " + Utils.getFullFormattedCurrency(presenter.getTransactionInfo().getAmount());
            confirmString += "\nTransfer Fee: " + Utils.getFullFormattedCurrency(fee);
            confirmString += "\nTotal: " + totalTextView.getText();
            confirmString += "\n\nWould you like to confirm this transaction?";

            MaterialDialog confirmTransactionDialog = new MaterialDialog.Builder(context)
                    .title("Confirm transaction")
                    .customView(R.layout.dialog_confirm_transaction, false)
                    .negativeText("No, take me back!")
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.dismiss();
                        }
                    })
                    .autoDismiss(false)
                    .positiveText("Yes!")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            View dialogView = dialog.getView();
                            PasscodeView passcodeView = dialogView.findViewById(R.id.passcodeView);
                            if (passcodeView.getText().toString().equals(Store.getPasscode())) {
                                if (Store.checkTransaction()) {
                                    Store.clearTransaction();
                                }
                                Store.saveRawTransaction(rawNumberText, amountString, message);
                                EventBus.getDefault().postSticky(new StartTransferServiceEvent(passcodeView.getText().toString()));
                                Intent serviceIntent = new Intent(getActivity(), TransactionService.class);
                                getActivity().startService(serviceIntent);
                                dialog.dismiss();
                                passcodeView.clearFocus();
                                KeyboardUtils.toggleSoftInput();
                                setTransactionProcessing(true);
                            } else {
                                passcodeView.clearText();
                                dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
                                Utils.shakeView(passcodeView);
                            }
                        }
                    })
                    .build();
            View dialogView = confirmTransactionDialog.getView();

            TextView confirmTransactionTextView = dialogView.findViewById(R.id.confirmTransactionTextView);
            confirmTransactionTextView.setText(confirmString);

            final View submitButton = confirmTransactionDialog.getActionButton(DialogAction.POSITIVE);
            submitButton.setEnabled(false);

            final PasscodeView passcodeView = dialogView.findViewById(R.id.passcodeView);
            passcodeView.setPasscodeEntryListener(new PasscodeView.PasscodeEntryListener() {
                @Override
                public void onPasscodeEntered(String passcode) {
                    if (passcode.length() == 6) {
                        submitButton.setEnabled(true);
                    }
                }
            });

            confirmTransactionDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            confirmTransactionDialog.show();

        }
    }

    TransferPresenter presenter;

    ContactsAdapter adapter;

    Context context;

    public TransferFragment() {
        // Required empty public constructor
    }

    public static TransferFragment newInstance(Context context) {
        TransferFragment fragment = new TransferFragment();
        fragment.context = context;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = new TransferPresenter(this, context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transfer, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            presenter.fetchContactsFromDatabase();
        } catch (Exception e) {
            Logger.d("Contacts permission may be missing.");
        }

        //EventBus.getDefault().post(new LoadingEvent(true));

        numberEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s == null || s.length() == 0) {
                    clearTextButton.setVisibility(GONE);
                } else if (s.length() > 0) {
                    clearTextButton.setVisibility(View.VISIBLE);
                }
                filterContacts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        numberEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (contactsCard.getVisibility() == GONE && presenter.getContactsData().size() > 0) {
                    showContacts();
                }
            }
        });

        numberEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    showContacts();
                    if (numberEditText.getText() != null && adapter != null)
                        adapter.filter(numberEditText.getText().toString());
                } else {
                    hideContacts();
                    if (numberEditText.getText() != null && numberEditText.getText().length() > 0)
                        presenter.checkTransactionInfo(numberEditText.getText().toString());
                    //setNumberEditText(false);
                }
            }
        });

        clearTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                numberEditText.setText("");
                numberEditText.requestFocus();
            }
        });

        amountLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                amountEditText.requestFocus();
                KeyboardUtils.showSoftInput(amountEditText);
                return true;
            }
        });

        totalTextView.setText(Utils.getFullFormattedCurrency(0f));

        amountEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                refreshTotalTextView(s.toString());
            }
        });

        contactsRecyclerView.setLayoutManager(new LinearLayoutManager(context));

    }

    @Override
    public void onResume() {
        super.onResume();

        if (Store.checkTransaction()) {
            JSONObject object = Store.getRawTransaction();
            try {
                numberEditText.setText(object.getString("number"));
                amountEditText.setText(object.getString("amount"));
                messageEditText.setText(object.getString("message"));
            } catch (Exception e) {
                Logger.e(e.toString());
            }
        }
        refreshTotalTextView(amountEditText.getText().toString());

        if (ServiceUtils.isServiceRunning("ezhr.epesa.TransactionService")) {
            setTransactionProcessing(true);
        }

        TransactionInfoEvent event = EventBus.getDefault().removeStickyEvent(TransactionInfoEvent.class);
        if (event != null) {
            String name = event.transactionInfo.getContactName();
            String phone = event.transactionInfo.getContactPhone();
            setNumberEditText(name, phone);
        }
    }



    private void showContacts() {
        Log.d(Utils.getTag(), "showContacts");
        contactsCard.setAlpha(0f);
        contactsCard.setVisibility(View.VISIBLE);
        contactsCard.animate()
                .alpha(1f)
                .setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime))
                .setListener(null);
    }

    private void hideContacts() {
        Log.d(Utils.getTag(), "hideContacts");
        numberEditText.clearFocus();
        KeyboardUtils.hideSoftInput(getActivity());
        contactsCard.animate()
                .alpha(0f)
                .setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime))
                .setListener(new Animator.AnimatorListener() {

                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        contactsCard.setVisibility(GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });
    }

    private void filterContacts(String s) {
        if (s != null && adapter != null)
            adapter.filter(s);
        contactsRecyclerView.getLayoutParams().height = RelativeLayout.LayoutParams.WRAP_CONTENT;
        contactsRecyclerView.invalidate();
    }

    @Override
    public void setNumberEditText(String name, String phone) {
        numberEditText.setText(name + " [" + phone + "]");
    }

    void loadQrScanner() {
        EventBus.getDefault().post(new SwitchViewEvent(MainActivityPagerAdapter.QRSCANNER_NUMBER, false));
    }

    @Override
    public void refreshContactsView() {
        adapter = new ContactsAdapter(R.layout.contacts_single_view, presenter.getContactsData());
        contactsRecyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                //String details = String.valueOf(presenter.getContactsData().get(position).getPhone());
                String name = presenter.getContactsData().get(position).getName();
                String phone = presenter.getContactsData().get(position).getPhone();
                //numberEditText.setText(details);
                numberEditText.clearFocus();
                KeyboardUtils.hideSoftInput(view);
                //setNumberEditText(true);
                setNumberEditText(name, phone);
            }
        });
        String filterText = numberEditText.getText().toString();
        if (filterText.length() > 0) {
            filterContacts(filterText);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTransactionCompleteEvent(TransactionCompleteEvent event) {
        if (event.success) {
            numberEditText.setText("");
            messageEditText.setText("");
            amountEditText.setText("");
        }
        setTransactionProcessing(false);
    }

    public void setTransactionProcessing(boolean isProcessing) {
        if (isProcessing) {
            sendMoneyButton.setText("PROCESSING TRANSACTION");
            sendMoneyButton.setEnabled(false);
            transferProgressView.setVisibility(View.VISIBLE);
            numberEditText.setEnabled(false);
            messageEditText.setEnabled(false);
            amountEditText.setEnabled(false);
        } else {
            sendMoneyButton.setText("TAP TO SEND");
            sendMoneyButton.setEnabled(true);
            transferProgressView.setVisibility(GONE);
            numberEditText.setEnabled(true);
            messageEditText.setEnabled(true);
            amountEditText.setEnabled(true);
        }
    }

    @Override
    public void refreshingContacts(boolean refreshing) {
        if (refreshing) {
            refreshContactsProgressView.setVisibility(View.VISIBLE);
        } else {
            refreshContactsProgressView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    private void refreshTotalTextView (String s) {
        float grandTotal;
        if (s.length() > 0) {
            float amount = Float.valueOf(amountEditText.getText().toString());
            if (feeTextView.getText().toString().contains("%")) {
                String feeString = feeTextView.getText().toString();
                feeString = feeString.substring(0, feeString.indexOf('%'));
                float feePercentage = Long.valueOf(feeString);
                fee = (feePercentage / 100) * amount;
            } else if (TextUtils.isDigitsOnly(feeTextView.getText().toString())) {
                String feeString = feeTextView.getText().toString();
                fee = Long.valueOf(feeString);
            } else {
                fee = 0f;
            }
            grandTotal = fee + amount;
        } else {
            grandTotal = 0f;
        }
        totalTextView.setText(Utils.getFullFormattedCurrency(grandTotal));
        //totalTextView.setText("abc");
    }
}
