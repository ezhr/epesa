package ezhr.epesa;

import android.app.NotificationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import ezhr.epesa.models.UserContact;

public class EpesaFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        final NotificationManager manager = Utils.getNotificationManager(this);

        final String notificationTitle = remoteMessage.getData().get("title");
        final int notificationIconResource = R.drawable.cash_multiple;
        final Uri notificationUri = Utils.getNotificationSound(this);

        final String amount = remoteMessage.getData().get("amount");

        final String sender = remoteMessage.getData().get("sender");

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {

                String notificationText = "You have received ";
                notificationText += Utils.getFullFormattedCurrency(Float.valueOf(amount));

                notificationText += " from ";

                UserContact contact = Utils.getRealmInstance().where(UserContact.class).equalTo("phone", sender).findFirst();
                if (contact != null) {
                    notificationText += contact.getName();
                } else {
                    notificationText += sender;
                }

                NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(EpesaFirebaseMessagingService.this, Utils
                        .defaultNotificationChannelId)
                        .setAutoCancel(true)
                        .setContentIntent(Utils.openAppIntent(EpesaFirebaseMessagingService.this))
                        .setContentTitle(notificationTitle)
                        .setContentText(notificationText)
                        .setSmallIcon(notificationIconResource)
                        .setSound(notificationUri, RingtoneManager.TYPE_NOTIFICATION)
                        .setOnlyAlertOnce(true)
                        .setVibrate(Utils.vibePattern)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(notificationText));

                manager.notify((int) System.currentTimeMillis(), nBuilder.build());

            }
        });
    }
}
