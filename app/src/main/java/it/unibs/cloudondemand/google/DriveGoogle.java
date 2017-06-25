package it.unibs.cloudondemand.google;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;


import it.unibs.cloudondemand.LoginActivity;

public class DriveGoogle extends LoginGoogle {
    private String CONTENT_TYPE;
    private String CONTENT;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent data=getIntent();

        CONTENT_TYPE=data.getStringExtra(LoginActivity.CONTENT_TYPE_EXTRA);
        CONTENT=data.getStringExtra(LoginActivity.CONTENT_EXTRA);

        //TODO move this in onConnected method
        if(CONTENT_TYPE!=LoginActivity.CONTENT_FOLDER) {
            Drive.DriveApi.newDriveContents(getGoogleApiClient())
                    .setResultCallback(driveContentsCallback);
        }
        else {
            //TODO
        }
    }

    //Called when file on Drive was created
    final private ResultCallback<DriveApi.DriveContentsResult> driveContentsCallback = new ResultCallback<DriveApi.DriveContentsResult>() {
        @Override
        public void onResult(@NonNull DriveApi.DriveContentsResult driveContentsResult) {
            if(!driveContentsResult.getStatus().isSuccess()) {
                Log.e("Google Drive", "Error while creating new file on Drive");
                return;
            }

            //Get content of new file
            final DriveContents driveContents = driveContentsResult.getDriveContents();

            //Upload file or string into drive file
            new Thread(){
                @Override
                public void run() {
                    //Choose stream to use by content type
                    switch (CONTENT_TYPE) {
                        case LoginActivity.CONTENT_STRING :

                            break;
                        case LoginActivity.CONTENT_FILE :

                            break;
                    }

                }
            }.start();
        }
    };

}