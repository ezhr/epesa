package ezhr.epesa;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.github.tamir7.contacts.Contact;
import com.github.tamir7.contacts.Contacts;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import ezhr.epesa.events.FetchTransactionsEvent;
import ezhr.epesa.events.LoadingEvent;
import ezhr.epesa.models.Transaction;
import ezhr.epesa.models.UserContact;
import io.realm.Realm;
import io.realm.RealmResults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by ezhr on 25/9/17.
 */

public class TransferPresenter {

    private TransferPresenterListener listener;
    private Context context;
    private Realm realm;
    private ArrayList<UserContact> contactsData = new ArrayList<>();
    private Transaction transactionInfo = new Transaction();

    public TransferPresenter(TransferFragment fragment, Context context) {
        this.listener = fragment;
        this.realm = Utils.getRealmInstance();
        this.context = context;
    }

    public ArrayList<UserContact> getContactsData() {
        return contactsData;
    }

    public Transaction getTransactionInfo() {
        return transactionInfo;
    }

    public void fetchContactsFromDatabase() {
        RealmResults<UserContact> userContacts = realm.where(UserContact.class).findAllSorted("name");
        contactsData.addAll(userContacts);
        EventBus.getDefault().post(new LoadingEvent(false));
        if (contactsData.size() == 0) {
            refreshContactsDatabase();
        } else {
            listener.refreshContactsView();
            EventBus.getDefault().post(new FetchTransactionsEvent(true));
        }
    }

    public void refreshContactsDatabase() {
        if (context != null) {
            listener.refreshingContacts(true);
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    Contacts.initialize(context);
                    List<Contact> rawContactsData = Contacts.getQuery().hasPhoneNumber().find();
                    contactsData.clear();
                    for (Contact contact : rawContactsData) {
                        for (int i = 0; i < contact.getPhoneNumbers().size(); i++) {
                            String name = contact.getDisplayName();
                            String number = contact.getPhoneNumbers().get(i).getNumber().replaceAll("\\D+", "");
                            contactsData.add(new UserContact(name, number));
                        }
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    checkContactsWithServer();
                }
            }.execute();
        }
    }

    public void checkContactsWithServer() {
        Call<List<UserContact>> call = Utils.getApi().checkContacts(Store.getToken(), contactsData);
        call.enqueue(new Callback<List<UserContact>>() {
            @Override
            public void onResponse(Call<List<UserContact>> call, final Response<List<UserContact>> response) {
                if (response.code() == 200) {
                    if (response.body() != null && response.body().size() > 0) {
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                realm.delete(UserContact.class);
                                realm.insertOrUpdate(response.body());
                            }
                        });
                    }
                    RealmResults<UserContact> userContacts = realm.where(UserContact.class).findAllSorted("name");
                    contactsData.clear();
                    contactsData.addAll(userContacts);
                    listener.refreshContactsView();
                    EventBus.getDefault().post(new FetchTransactionsEvent(true));
                    listener.refreshingContacts(false);
                } else {
                    Utils.displayWarning(context, response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<List<UserContact>> call, Throwable t) {
                Logger.e(t.toString());
                Utils.displayError(context, t);
                listener.refreshingContacts(false);
            }
        });
    }

    public void checkTransactionInfo(String s) {
        if (TextUtils.isDigitsOnly(s)) {
            UserContact contact = realm.where(UserContact.class).equalTo("phone", s).findFirst();
            if (contact != null) {
                String name = contact.getName();
                String phone = contact.getPhone();
                listener.setNumberEditText(name, phone);
            }
        }
    }

    public void setTransactionInfo(Transaction transactionInfo) {
        this.transactionInfo = transactionInfo;
    }

    public interface TransferPresenterListener {
        void refreshContactsView();
        void setNumberEditText(String name, String phone);
        void refreshingContacts(boolean refreshing);
    }

}
