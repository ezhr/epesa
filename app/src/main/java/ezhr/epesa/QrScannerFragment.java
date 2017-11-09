package ezhr.epesa;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.EncodeUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.zxing.Result;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import ezhr.epesa.events.TransactionInfoEvent;
import ezhr.epesa.events.ScannerEvent;
import ezhr.epesa.models.Transaction;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class QrScannerFragment extends Fragment {

    @BindView(R.id.qrScannerView)
    ZXingScannerView scanner;
    static QrScannerFragment fragment;
    Context context;

    public QrScannerFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public static QrScannerFragment newInstance (Context context) {
        if (fragment == null) {
            fragment = new QrScannerFragment();
            fragment.context = context;
        }
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_qr_scanner, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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

    @Subscribe (threadMode = ThreadMode.MAIN)
    public void onScannerEvent (ScannerEvent event) {
        if (event.start) {
            scanner.startCamera();
            scanner.setResultHandler(new ZXingScannerView.ResultHandler() {
                @Override
                public void handleResult(Result result) {
                    try {
                        //Toast.makeText(context, result.toString(), Toast.LENGTH_LONG).show();
                        byte[] bytes = EncodeUtils.base64Decode(result.toString());
                        char[] characters = ConvertUtils.bytes2Chars(bytes);
                        String decoded = String.copyValueOf(characters);
                        JSONObject jsonobject = new JSONObject(decoded);
                        Gson gson = new GsonBuilder().create();
                        Transaction userTransaction = gson.fromJson(jsonobject.toString(), Transaction.class);
                        //Toast.makeText(context, userTransaction.getContactName(), Toast.LENGTH_LONG).show();
                        scanner.stopCamera();
                        EventBus.getDefault().postSticky(new TransactionInfoEvent(userTransaction));
                    } catch (Exception e) {
                        Log.e(Utils.getTag(), e.toString() + " " + result.getText());
                        scanner.resumeCameraPreview(this);
                    }
                }
            });
        } else {
            scanner.stopCamera();
        }
    }
}
