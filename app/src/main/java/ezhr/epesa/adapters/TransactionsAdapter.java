package ezhr.epesa.adapters;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import org.joda.time.DateTime;

import java.text.NumberFormat;
import java.util.List;

import ezhr.epesa.R;
import ezhr.epesa.Store;
import ezhr.epesa.Utils;
import ezhr.epesa.models.Transaction;
import ezhr.epesa.models.UserContact;
import io.realm.Realm;
import io.realm.RealmResults;

import static android.view.View.GONE;

/**
 * Created by ezhr on 12/9/17.
 */

public class TransactionsAdapter extends BaseQuickAdapter<Transaction, BaseViewHolder> {
    Context context;

    public TransactionsAdapter(@LayoutRes int layoutResId, @Nullable List<Transaction> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, Transaction item) {

        String dateString = "";
        String nameString;


    /*    new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {*/
        DateTime dateTime = new DateTime(item.getDate());

        switch (dateTime.getMonthOfYear()) {
            case 1:
                dateString += "Jan";
                break;
            case 2:
                dateString += "Feb";
                break;
            case 3:
                dateString += "Mar";
                break;
            case 4:
                dateString += "Apr";
                break;
            case 5:
                dateString += "May";
                break;
            case 6:
                dateString += "Jun";
                break;
            case 7:
                dateString += "Jul";
                break;
            case 8:
                dateString += "Aug";
                break;
            case 9:
                dateString += "Sep";
                break;
            case 10:
                dateString += "Oct";
                break;
            case 11:
                dateString += "Nov";
                break;
            case 12:
                dateString += "Dec";
                break;
        }

        String hour;
        if (dateTime.getHourOfDay() % 12 == 0) {
            hour = "12";
        } else {
            hour = String.valueOf(dateTime.getHourOfDay() % 12);
        }


        dateString += " " + dateTime.getDayOfMonth() + ", " + dateTime.getYear();
        dateString += " - " + hour + ":" + Utils.getFullMinutes(dateTime.getMinuteOfHour()) + " ";

        if (dateTime.getHourOfDay() / 12 == 0) {
            dateString += "AM";
        } else {
            dateString += "PM";
        }

        Realm realm = Realm.getDefaultInstance();
        UserContact contact = realm.where(UserContact.class).equalTo("phone", item.getContactPhone()).findFirst();
        if (contact == null) {
            nameString = item.getContactPhone();
        } else {
            nameString = contact.getName();
        }

        helper.setText(R.id.singleTransactionDate, dateString);

        if (item.wasSent()) {
            helper.setText(R.id.singleTransactionType, "sent");
            helper.setText(R.id.singleTransactionAmount, "- " + Utils.getFullFormattedCurrency(item.getAmount()));
            helper.setTextColor(R.id.singleTransactionAmount, ContextCompat.getColor(context, R.color.red));
        } else {
            helper.setText(R.id.singleTransactionType, "received");
            helper.setText(R.id.singleTransactionAmount, "+ " + Utils.getFullFormattedCurrency(item.getAmount()));
            helper.setTextColor(R.id.singleTransactionAmount, ContextCompat.getColor(context, R.color.green));
        }

        helper.setText(R.id.transactionsNameText, nameString);

        if (item.getMessage() != null) {
            helper.setText(R.id.transactionsMessageText, item.getMessage());
        } else {
            helper.getView(R.id.transactionsMessageText).setVisibility(GONE);
        }
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
