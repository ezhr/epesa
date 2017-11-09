package ezhr.epesa.adapters;

import android.os.AsyncTask;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.ArrayList;
import java.util.List;

import ezhr.epesa.R;
import ezhr.epesa.Store;
import ezhr.epesa.Utils;
import ezhr.epesa.models.UserContact;
import io.realm.Realm;

/**
 * Created by ezhr on 11/9/17.
 */

public class ContactsAdapter extends BaseQuickAdapter<UserContact, BaseViewHolder> {
    List<UserContact> data = new ArrayList<>();
    List<UserContact> dataCopy = new ArrayList<>();



    public ContactsAdapter(@LayoutRes int layoutResId, @Nullable List<UserContact> data) {
        super(layoutResId, data);
        this.data = data;
        dataCopy.addAll(data);
    }

    @Override
    protected void convert(BaseViewHolder helper, UserContact item) {
        helper.setText(R.id.contactsViewName, item.getName());
        String phone = item.getPhone();
        String countryCode = Store.getCountryCode();
        if (phone.startsWith(countryCode)) {
            phone = phone.substring(countryCode.length());
            phone = "0" + phone;
        }
        helper.setText(R.id.contactsViewNumber, phone);
    }

    public void filter (String query) {
        data.clear();
        if (query.isEmpty()) {
            data.addAll(dataCopy);
        } else {

            for (UserContact userContact : dataCopy) {
                String toSearch;
                if (TextUtils.isDigitsOnly(query)) {
                    toSearch = String.valueOf(userContact.getPhone());
                } else {
                    query = query.toLowerCase();
                    toSearch = userContact.getName().toLowerCase();

                }
                if (toSearch.contains(query)) {
                    data.add(userContact);
                }
            }
        }
        notifyDataSetChanged();
    }
}
