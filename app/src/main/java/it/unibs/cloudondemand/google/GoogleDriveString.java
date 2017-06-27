package it.unibs.cloudondemand.google;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
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

import it.unibs.cloudondemand.LoginActivity;

public class GoogleDriveString extends GoogleDrive {
    private static final String TAG = "GoogleDriveUpString";

    @Override
    public void onConnected() {
        Drive.DriveApi.newDriveContents(getGoogleApiClient())
                .setResultCallback(driveContentsCallback);
    }

    //Called when new content on Drive was created
    final private ResultCallback<DriveApi.DriveContentsResult> driveContentsCallback = new ResultCallback<DriveApi.DriveContentsResult>() {
        @Override
        public void onResult(@NonNull DriveApi.DriveContentsResult driveContentsResult) {
            if (!driveContentsResult.getStatus().isSuccess()) {
                Log.e(TAG, "Error while creating new file on Drive");
                return;
            }

            //Get content of new file
            final DriveContents driveContents = driveContentsResult.getDriveContents();


            //Upload file or string into drive file
            new Thread() {
                @Override
                public void run() {
                    //Create stream based on which data need to be saved
                    Writer writer = new OutputStreamWriter(driveContents.getOutputStream());
                    try {
                        writer.write(getContent());
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            writer.close();
                        } catch (Exception e) {
                            Log.e(TAG, "Exception while writing on driveConetents output stream", e.getCause());
                        }
                    }

                    MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                            .setTitle("nome")
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


    final private ResultCallback<DriveFolder.DriveFileResult> fileCallback = new ResultCallback<DriveFolder.DriveFileResult>() {
        @Override
        public void onResult(@NonNull DriveFolder.DriveFileResult driveFileResult) {
            if(!driveFileResult.getStatus().isSuccess()) {
                Toast.makeText(GoogleDriveString.this, "File non Creato", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "File not created");
            }
            else {
                Toast.makeText(GoogleDriveString.this, "File Creato", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "File created. " + driveFileResult.getDriveFile().getDriveId());
            }

            getGoogleApiClient().disconnect();
        }
    };
}
