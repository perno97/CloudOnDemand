package it.unibs.cloudondemand.google;

import android.app.Notification;

/**
 * Created by Fabio on 05/09/2017.
 */

public class GoogleDriveDownloadFile extends GoogleDriveConnection {

    private static final String TAG = "GoogleDriveUpFile";

    @Override
    public void onConnected() {

    }

    @Override
    public Notification getFinalNotification() {
        return null;
    }
}
