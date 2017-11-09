package ezhr.epesa;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.appolica.flubber.Flubber;
import com.appolica.flubber.annotations.RepeatMode;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.IntentUtils;
import com.google.firebase.iid.FirebaseInstanceId;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.orhanobut.logger.Logger;

import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;
import ezhr.epesa.models.StandardResponses;
import ezhr.epesa.models.Transaction;
import ezhr.epesa.models.UserContact;
import io.realm.Realm;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by ezhr on 6/9/17.
 */

public class Utils {

    private static final String BASE_URL = "http://127.0.0.1:3000/"; // Edit this to the URL where the epesa-js app is hosted.
    private static Retrofit retrofit;
    private static APIInterface api;
    private static final String TAG = "ezhrlog";
    private static Realm realm;

    public static final String transactionServiceName = "TransactionService";
    public static final String ongoingNotificationChannelId = "epesa_channel_00";
    public static final String defaultNotificationChannelId = "epesa_channel_01";
    public static final long[] vibePattern = new long[]{0, 350, 200, 350};

    private static void setup() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        // set your desired log level
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        // add your other interceptors …

        // add logging as last interceptor
        httpClient.addInterceptor(logging);  // <-- this is the important line!
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();
        api = retrofit.create(APIInterface.class);
    }

    public static APIInterface getApi() {
        if (api == null) {
            setup();
        }
        return api;
    }

    public static Realm getRealmInstance() {
        if (realm == null)
            realm = Realm.getDefaultInstance();
        return realm;
    }

    public static String getTag() {
        return TAG;
    }

    public static String getFullFormattedCurrency(float value) {
        DecimalFormat currencyFormatter = new DecimalFormat("###,###,###,##0.00");
        return "Tsh " + currencyFormatter.format(value);
    }

    public static String getFullMinutes(int minutes) {
        DecimalFormat minuteFormatter = new DecimalFormat("00");
        return minuteFormatter.format(minutes);
    }

    public static String getFormattedCardNumber(String number) {
        String formatted = "";
        while (number.length() > 0) {
            if (number.length() > 3) {
                formatted += number.substring(0, 4) + " ";
                number = number.substring(4);
            } else {
                formatted += number;
                number = "";
            }
        }
        return formatted;
    }

    public static void shakeView(View view) {
        Flubber.with()
                .animation(Flubber.AnimationPreset.SHAKE)
                .force(0.75f)
                .repeatCount(0)
                .duration(550)
                .createFor(view)
                .start();
    }

    public static void acquirePermissions(final Activity activity) {
        Dexter.withActivity(activity)
                .withPermissions(Manifest.permission.READ_CONTACTS,
                        Manifest.permission.CAMERA,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            new MaterialDialog.Builder(activity)
                                    .title("Missing Permissions!")
                                    .content("Unfortunately, epesa cannot function without the required permissions. Please grant them in" +
                                            " Settings.")
                                    .positiveText("Open Settings")
                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            activity.startActivity(IntentUtils.getAppDetailsSettingsIntent(activity.getApplication()
                                                    .getPackageName()));
                                        }
                                    })
                                    .negativeText("Quit epesa")
                                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            AppUtils.exitApp();
                                        }
                                    })
                                    .cancelable(false)
                                    .build().show();
                            return;
                        }
                        if (!report.areAllPermissionsGranted()) {
                            acquirePermissions(activity);
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, final PermissionToken token) {
                        new MaterialDialog.Builder(activity)
                                .title("Whoops!")
                                .content("Looks like some permissions are missing in order for epesa to work correctly. Please grant " +
                                        "them in the dialog that follows.")
                                .positiveText("Sure")
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        token.continuePermissionRequest();
                                        dialog.dismiss();
                                    }
                                })
                                .negativeText("Quit epesa")
                                .onNegative(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        token.cancelPermissionRequest();
                                        activity.finishAffinity();
                                    }
                                })
                                .cancelable(false)
                                .build().show();
                    }
                })
                .withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError error) {
                        Logger.d("ERROR " + error);
                    }
                })
                .check();

    }

    public static void logOut(final Activity activity) {
        Store.clearAll();
        String qrFilePath = activity.getFilesDir() + "/qr";
        FileUtils.deleteFile(qrFilePath);
        Realm realm = Utils.getRealmInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.delete(UserContact.class);
                realm.delete(Transaction.class);
            }
        });
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    FirebaseInstanceId.getInstance().deleteInstanceId();
                } catch (IOException e) {
                    Logger.e(e.toString());
                }
                return null;
            }
        }.execute();
    }

    public static Transaction parseTransaction (String rawNumberText, String amountString, String message) {
        String name = "";
        String phone;

        amountString = amountString.replaceAll("[^\\d.]", "");
        float amount = Float.valueOf(amountString);

        if (TextUtils.isDigitsOnly(rawNumberText)) {
            phone = rawNumberText;
        } else {
            int startIndex = rawNumberText.indexOf('[') + 1;
            int endIndex = rawNumberText.indexOf(']');
            phone = rawNumberText.substring(startIndex, endIndex);
            if (!TextUtils.isDigitsOnly(phone)) {
                return null;
            }
            name = rawNumberText.substring(0, startIndex - 2);
        }
        Transaction transactionInfo = new Transaction(name, phone, amount, message);
        return transactionInfo;
    }

    public static void displayWarning (Context context, String errorString) {
        Toasty.warning(context, errorString, Toast.LENGTH_SHORT).show();
    }

    public static void displayWarning(Context context, ResponseBody response) {
        try {
            Toasty.warning(context, response.string(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Logger.d(e.toString());
        }
    }

    public static void displayError (Context context, Throwable t) {
        String error = t.toString();
        if (error.contains("java.net.ConnectException")) {
            error = "Failed to connect to server. Are you sure your phone is connected to the internet?";
        }
        Toasty.error(context, error, Toast.LENGTH_SHORT).show();
    }

    public static void displayError (Context context, String error) {
        if (error.contains("java.net.ConnectException")) {
            error = "Failed to connect to server. Are you sure your phone is connected to the internet?";
        }
        Toasty.error(context, error, Toast.LENGTH_SHORT).show();
    }

    public static void displaySuccess (Context context, Response<StandardResponses> response) {
        Toasty.success(context, response.body().getMessage(), Toast.LENGTH_SHORT).show();
    }

    public static void displaySuccess (Context context, String successString) {
        Toasty.success(context, successString, Toast.LENGTH_SHORT).show();
    }

    public static void setProgressColor(Context context, ProgressBar view, int color) {
        view.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(context, color), PorterDuff.Mode.MULTIPLY);
    }

    public static Uri getNotificationSound (Context context) {
        return Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.notification);
    }

    public static Uri getErrorNotificationSound (Context context) {
        return Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.errornotification);
    }

    public static PendingIntent openAppIntent (Context context) {

        Intent onNotificationClickIntent = new Intent(context, MainActivity.class);

        PendingIntent pIntent = PendingIntent.getActivity(
                context,
                (int) System.currentTimeMillis(),
                onNotificationClickIntent,
                0
        );
        return pIntent;
    }

    public static NotificationManager getNotificationManager (Context context) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            setupNotificationChannels(manager);
        }
        return manager;
    }


    private static void setupNotificationChannels(NotificationManager manager) {

        CharSequence completedChannelName = "epesa Completed Transactions";
        int completedChannelImportance = NotificationManagerCompat.IMPORTANCE_DEFAULT;

        CharSequence ongoingChannelName = "epesa Ongoing Transactions";
        int ongoingChannelImportance = NotificationManagerCompat.IMPORTANCE_HIGH;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel ongoingChannel = new NotificationChannel(Utils.defaultNotificationChannelId, ongoingChannelName, ongoingChannelImportance);
            manager.createNotificationChannel(ongoingChannel);
            NotificationChannel completedChannel = new NotificationChannel(Utils.defaultNotificationChannelId, completedChannelName,
                    completedChannelImportance);
            manager.createNotificationChannel(completedChannel);


        }

    }
}
