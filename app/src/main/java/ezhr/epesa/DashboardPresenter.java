package ezhr.epesa;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.AsyncTask;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.EncodeUtils;
import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.ImageUtils;
import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;

import ezhr.epesa.models.Transaction;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by ezhr on 25/9/17.
 */

public class DashboardPresenter {

    DashboardPresenterListener listener;

    String phone = "";
    String name = "";
    String qrFilePath;
    Context context;

    ArrayList<Transaction> transactionsData = new ArrayList<>();

    private Realm realm = Utils.getRealmInstance();

    public DashboardPresenter(DashboardFragment fragment, Context context) {
        this.listener = fragment;
        this.context = context;
    }

    public void getUserInfo() {
        phone = Store.getUserPhone();
        name = Store.getUserName();
        String phoneString = Utils.getFormattedCardNumber(phone);
        listener.configureUserCardInfo(phoneString, name);
    }

    public void getUserQr(final Activity activity) {
        qrFilePath = activity.getFilesDir() + "/qr";
        new AsyncTask<Void, Void, byte[]>() {
            @Override
            protected byte[] doInBackground(Void... voids) {
                byte[] rawQr = FileIOUtils.readFile2BytesByStream(qrFilePath);
                return rawQr;
            }

            @Override
            protected void onPostExecute(byte[] rawQr) {
                if (rawQr == null) {
                    generateUserQr();
                } else {
                    Bitmap qr = ConvertUtils.bytes2Bitmap(rawQr);
                    listener.configureUserCardQr(qr);
                }
            }
        }.execute();
    }

    private void generateUserQr() {
        if (!Store.checkUserName() | !Store.checkUserPhone()) {
            return;
        }
        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... voids) {
                Bitmap logo = ImageUtils.getBitmap(R.drawable.epesa_qr_logo);

                Bitmap qr;
                BitMatrix matrix = null;

                Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap = new Hashtable<>();
                hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);

                Transaction userInfo = new Transaction(name, phone);
                String qrString = new Gson().toJson(userInfo);
                qrString = EncodeUtils.base64Encode2String(qrString.getBytes());

                try {
                    matrix = new QRCodeWriter().encode(qrString,
                            BarcodeFormat.QR_CODE, 900, 900, hintMap);
                } catch (WriterException e) {
                    e.printStackTrace();
                }

                int height = matrix.getHeight();
                int width = matrix.getWidth();
                qr = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        qr.setPixel(x, y, matrix.get(x, y) ? Color.BLACK : Color.WHITE);
                    }
                }

                Bitmap finalQr = Bitmap.createBitmap(width, height, qr.getConfig());
                Canvas canvas = new Canvas(finalQr);
                int canvasWidth = canvas.getWidth();
                int canvasHeight = canvas.getHeight();

                canvas.drawBitmap(qr, new Matrix(), null);

                int centreX = (canvasWidth - logo.getWidth()) / 2;
                int centreY = (canvasHeight - logo.getHeight()) / 2;
                canvas.drawBitmap(logo, centreX, centreY, null);

                byte[] rawQr = ConvertUtils.bitmap2Bytes(finalQr, Bitmap.CompressFormat.PNG);
                FileIOUtils.writeFileFromBytesByStream(qrFilePath, rawQr);

                return finalQr;
            }

            @Override
            protected void onPostExecute(Bitmap qr) {
                listener.configureUserCardQr(qr);
            }
        }.execute();
    }

    public ArrayList<Transaction> getTransactionsData() {
        return transactionsData;
    }

    public void getTransactions() {
        RealmResults<Transaction> transactions = realm.where(Transaction.class).findAllSorted("date", Sort.DESCENDING);
        long from = 0;
        if (transactions == null) {
            Logger.d("No transactions in database.");
        } else if (transactions.size() > 0) {
            transactionsData.clear();
            transactionsData.addAll(transactions);
            listener.refreshTransactions();
        }
        fetchTransactionsFromDatabase(from);

    }

    public void fetchTransactionsFromDatabase(long from) {

        listener.activityProgress(true);
        if (transactionsData.size() > 0) {
            from = transactionsData.get(0).getDate();
        }

        //todo sort responses
        Call<List<Transaction>> call = Utils.getApi().getUserTransactions(Store.getToken(), from);
        call.enqueue(new Callback<List<Transaction>>() {
            @Override
            public void onResponse(Call<List<Transaction>> call, final Response<List<Transaction>> response) {
                if (response.code() == 200) {

                    if (response.body() != null && response.body().size() > 0) {
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                realm.insertOrUpdate(response.body());
                                transactionsData.addAll(response.body());
                                Collections.sort(transactionsData, new Comparator<Transaction>() {
                                    @Override
                                    public int compare(Transaction t1, Transaction t2) {
                                        return (int) (t2.getDate() - t1.getDate());
                                    }
                                });
                                listener.refreshTransactions();
                            }
                        });
                    } else {
                        Logger.d("No more transactions.");
                    }
                } else {
                    Utils.displayWarning(context, response.errorBody());
                }
                listener.activityProgress(false);
            }
            @Override
            public void onFailure(Call<List<Transaction>> call, Throwable t) {
                Logger.e(t.toString());
                Utils.displayError(context, t);
                listener.activityProgress(false);
            }
        });
    }

    // Helper method to clear database
    public void clearDatabase() {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.delete(Transaction.class);
            }
        });
        transactionsData.clear();
        listener.refreshTransactions();
    }

    public interface DashboardPresenterListener {
        void configureUserCardInfo(String phoneString, String name);

        void configureUserCardQr(Bitmap userQr);

        void refreshTransactions();

        void activityProgress(boolean loading);
    }
}
