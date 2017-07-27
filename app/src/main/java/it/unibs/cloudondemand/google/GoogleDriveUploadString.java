package it.unibs.cloudondemand.google;

import android.app.Notification;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.MetadataChangeSet;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import it.unibs.cloudondemand.R;

public class GoogleDriveUploadString extends GoogleDriveConnection {
    private static final String TAG = "GoogleDriveUpString";


    @Override
    public void onConnected() {
        Drive.DriveApi.newDriveContents(getGoogleApiClient())
                .setResultCallback(driveContentsCallback);

        showNotification(this, 0, true);
    }

    // Called when new content on Drive was created
    final private ResultCallback<DriveApi.DriveContentsResult> driveContentsCallback = new ResultCallback<DriveApi.DriveContentsResult>() {
        @Override
        public void onResult(@NonNull DriveApi.DriveContentsResult driveContentsResult) {
            if (!driveContentsResult.getStatus().isSuccess()) {
                Log.e(TAG, "Error while creating new file on Drive");
                return;
            }

            // Get content of new file
            final DriveContents driveContents = driveContentsResult.getDriveContents();


            // Upload string into drive content
            new Thread() {
                @Override
                public void run() {
                    //Create stream based on which data need to be saved
                    Writer writer = new OutputStreamWriter(driveContents.getOutputStream());
                    try {
                        writer.write(getContent());
                    } catch (IOException e) {
                        Log.e(TAG, "Exception while writing on DriveConetents output stream.\n" + e.toString(), e.getCause());
                    } finally {
                        try {
                            writer.close();
                        } catch (Exception e) {
                            Log.e(TAG, "Exception while closing driveConetents output stream", e.getCause());
                        }
                    }

                    MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                            .setTitle("String.txt")
                            .setMimeType("text/plain")
                            .setStarred(true)
                            .build();

                    Drive.DriveApi.getRootFolder(getGoogleApiClient())
                            .createFile(getGoogleApiClient(), changeSet, driveContents)
                            .setResultCallback(fileCallback);
                }
            }.start();
        }
    };

    // Called when file on drive was fully created
    final private ResultCallback<DriveFolder.DriveFileResult> fileCallback = new ResultCallback<DriveFolder.DriveFileResult>() {
        @Override
        public void onResult(@NonNull DriveFolder.DriveFileResult driveFileResult) {
            if(!driveFileResult.getStatus().isSuccess()) {
                Toast.makeText(GoogleDriveUploadString.this, "File non Creato", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "File not created");
            }
            else {
                Log.i(TAG, "File created. " + driveFileResult.getDriveFile().getDriveId());
            }

            disconnect();
        }
    };

    @Override
    public Notification getFinalNotification() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(GoogleDriveUploadString.this)
                        .setSmallIcon(GoogleDriveConnection.NOTIFICATION_ICON)
                        .setContentTitle("Uploading file to Drive...") //TODO mettere dentro res/values
                        .setContentText("Finito");

        return mBuilder.build();
    }

    @Override
    public int getStopServiceExtra() {
        return StopServices.SERVICE_UPLOAD_STRING;
    }
}
