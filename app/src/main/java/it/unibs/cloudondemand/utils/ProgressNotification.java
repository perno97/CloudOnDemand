package it.unibs.cloudondemand.utils;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import it.unibs.cloudondemand.R;

public class ProgressNotification {
    // Small icon for notification
    public static final int NOTIFICATION_ICON = R.mipmap.ic_launcher;

    private Context context;
    private NotificationCompat.Builder mNotificationBuilder;
    //TODO IMPLEMENTARE TITOLO NOTIFICA
    /**
     * Create a new progress notification with stop intent.
     * @param context A context.
     * @param contentText Content of notifcation.
     * @param indeterminateProgress True if there isn't a progress value.
     * @param stopIntent Intent to launch when click the stop button.
     */
    public ProgressNotification(Context context, String titleText, String contentText, boolean indeterminateProgress, Intent stopIntent) {
        this.context = context;
        createNewNotification(titleText, contentText, indeterminateProgress, stopIntent);
    }

    /**
     * Create a new progress notification without stop intent.
     * @param context A context.
     * @param contentText Content of notifcation.
     * @param indeterminateProgress True if there isn't a progress value.
     */
    public ProgressNotification(Context context, String titleText, String contentText, boolean indeterminateProgress) {
        this.context = context;
        createNewNotification(titleText, contentText, indeterminateProgress, null);
    }

    // Initialize first time the notification builder
    private NotificationCompat.Builder createNewNotification(String titleText, String contentText, boolean indeterminateProgress, Intent stopIntent) {
        mNotificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(NOTIFICATION_ICON)
                .setContentTitle(titleText)
                .setContentText(contentText)
                .setProgress(100, 0, indeterminateProgress)
                .setOngoing(true);

        // Add action to notification
        if(stopIntent != null) {
            // Intent to launch when stop pressed
            PendingIntent pendingIntent = PendingIntent.getService(context, 0, stopIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            // Add pending intent to notification builder
            if (Build.VERSION.SDK_INT > 23) {
                NotificationCompat.Action stopAction = new NotificationCompat.Action.Builder(R.drawable.ic_close, "Stop", pendingIntent).build();
                mNotificationBuilder.addAction(stopAction);
            } else {
                mNotificationBuilder.addAction(R.drawable.ic_close, "Stop", pendingIntent);
            }
        }

        return mNotificationBuilder;
    }

    /**
     * Edit already created notification with new text/progress value.
     * @param progress New progress value for bar.
     * @param contentText New content text of notification.
     * @return New created notification.
     */
    public Notification editNotification (int progress, String contentText) {
        if(contentText == null)
            mNotificationBuilder.setProgress(100, progress, false);
        else
            mNotificationBuilder.setProgress(100, progress, false)
                    .setContentText(contentText);

        return mNotificationBuilder.build();
    }

    /**
     * Edit already created notification with new progress value.
     * @param progress New progress value for bar.
     * @return New created notification.
     */
    public Notification editNotification (int progress) {
        mNotificationBuilder.setProgress(100, progress, false);

        return mNotificationBuilder.build();
    }

    /**
     * Getter current notification.
     * @return Current notification.
     */
    public Notification getNotification() {
        return mNotificationBuilder.build();
    }
}
