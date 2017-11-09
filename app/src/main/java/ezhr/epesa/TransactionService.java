package ezhr.epesa;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.blankj.utilcode.util.AppUtils;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;

import ezhr.epesa.events.StartTransferServiceEvent;
import ezhr.epesa.events.TransactionCompleteEvent;
import ezhr.epesa.models.StandardResponses;
import ezhr.epesa.models.Transaction;
import ezhr.epesa.models.TransferBody;
import retrofit2.Call;
import retrofit2.Response;

public class TransactionService extends IntentService {

    NotificationManager nManager;
    final int ongoingNotificationId = 0;

    public TransactionService() {
        super(Utils.transactionServiceName);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        StartTransferServiceEvent event = EventBus.getDefault().removeStickyEvent(StartTransferServiceEvent.class);

        nManager = Utils.getNotificationManager(this);

        NotificationCompat.Builder ongoingNotification = new NotificationCompat.Builder(this, Utils.ongoingNotificationChannelId)
                .setOngoing(true)
                .setContentTitle("Processing transaction ...")
                .setPriority(NotificationManagerCompat.IMPORTANCE_HIGH)
                .setProgress(100, 100, true)
                .setContentIntent(Utils.openAppIntent(this))
                .setSmallIcon(R.drawable.processing);

        nManager.notify(ongoingNotificationId, ongoingNotification.build());

        if (!Store.checkTransaction()) {
            Logger.e("NO TRANSACTION INFO");
            return;
        }

        Transaction transactionInfo = Store.getTransaction();
        String passcode = event.passcode;
        if (transactionInfo == null) {
            // TODO: Implement graceful error handling and return
        }

        TransferBody transferBody = new TransferBody(
                transactionInfo.getAmount(),
                transactionInfo.getMessage()
        );

        Call<StandardResponses> call = Utils.getApi().transfer(Store.getToken(), passcode, transactionInfo.getContactPhone(),
                transferBody);

        String notificationTitle = "Transaction failed!";
        String notificationText;
        int notificationIconResource = R.drawable.failed;
        Uri notificationUri = Utils.getErrorNotificationSound(this);

        try {
            final Response<StandardResponses> response = call.execute();

            if (response.code() == 200) {
                //EventBus.getDefault().postSticky(new SwitchViewEvent(MainActivityPagerAdapter.DASHBOARD_NUMBER, false));
                Store.clearTransaction();
                if (AppUtils.isAppForeground()) {
                    EventBus.getDefault().post(new TransactionCompleteEvent(true, response.body().getMessage()));
                }
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.displaySuccess(TransactionService.this, "epesa Transaction Complete!");
                    }
                });

                notificationTitle = "Transaction successful!";
                notificationText = response.body().getMessage();
                notificationIconResource = R.drawable.success;

                notificationUri = Utils.getNotificationSound(this);

            } else {
                // Todo error handling from the server
                //EventBus.getDefault().postSticky(new SwitchViewEvent(MainActivityPagerAdapter.TRANSFER_NUMBER, false));
                final String errorString = response.errorBody().string();
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.displayWarning(TransactionService.this, "epesa Transaction Failed!");
                    }
                });

                if (AppUtils.isAppForeground()) {
                    EventBus.getDefault().post(new TransactionCompleteEvent(false, null));
                }

                notificationText = errorString;

            }

        } catch (final IOException e) {
            // Todo break the service, trigger resending
            Logger.e(e.toString());
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Utils.displayError(TransactionService.this, e.toString());
                }
            });
            if (e.toString().contains("java.net.ConnectException")) {
                notificationText = "Failed to connect to server. Please try again later.";
            } else {
                notificationText = e.toString();
            }

            if (AppUtils.isAppForeground()) {
                EventBus.getDefault().post(new TransactionCompleteEvent(false, null));
            }
        }

        nManager.cancel(ongoingNotificationId);

        NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(this, Utils.defaultNotificationChannelId)
                .setAutoCancel(true)
                .setContentIntent(Utils.openAppIntent(this))
                .setContentTitle(notificationTitle)
                .setContentText(notificationText)
                .setSmallIcon(notificationIconResource)
                .setSound(notificationUri, RingtoneManager.TYPE_NOTIFICATION)
                .setOnlyAlertOnce(true)
                .setVibrate(Utils.vibePattern)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(notificationText));

        nManager.notify((int) System.currentTimeMillis(), nBuilder.build());
    }
}
