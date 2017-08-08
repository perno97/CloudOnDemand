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

    public ProgressNotification(Context context, String contentText, boolean indeterminateProgress, Intent stopIntent) {
        this.context = context;
        createNewNotification(contentText, indeterminateProgress, stopIntent);
    }

    // Without stop action
    public ProgressNotification(Context context, String contentText, boolean indeterminateProgress) {
        this.context = context;
        createNewNotification(contentText, indeterminateProgress, null);
    }

    private NotificationCompat.Builder createNewNotification(String contentText, boolean indeterminateProgress, Intent stopIntent) {
        mNotificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(NOTIFICATION_ICON)
                .setContentTitle("Uploading files to Drive...") //TODO res/strings
                .setContentText(contentText)
                .setProgress(100, 0, indeterminateProgress)
                //.addAction()
                .setOngoing(true);

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

    public Notification editNotification (int progress, String contentText) {
        if(contentText == null)
            mNotificationBuilder.setProgress(100, progress, false);
        else
            mNotificationBuilder.setProgress(100, progress, false)
                    .setContentText(contentText);

        return mNotificationBuilder.build();
    }

    public Notification editNotification (int progress) {
        mNotificationBuilder.setProgress(100, progress, false);

        return mNotificationBuilder.build();
    }

    public Notification getNotification() {
        return mNotificationBuilder.build();
    }
}
