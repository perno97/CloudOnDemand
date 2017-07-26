package it.unibs.cloudondemand.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.Iterator;
import java.util.List;

import it.unibs.cloudondemand.LoginActivity;
import it.unibs.cloudondemand.google.GoogleDriveConnection;
import it.unibs.cloudondemand.google.GoogleDriveUploadFileFolder;
import it.unibs.cloudondemand.google.GoogleDriveUploadFileSingle;
import it.unibs.cloudondemand.google.GoogleDriveUploadString;

public class StopServices extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setMessage("Vuoi fermare il caricamento?")   //TODO spostare in values/string
                .setTitle("Stop")
                .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i("Notification", "Stopping service.");

                        stopService(new Intent(StopServices.this, GoogleDriveUploadString.class));
                        stopService(new Intent(StopServices.this, GoogleDriveUploadFileSingle.class));
                        stopService(new Intent(StopServices.this, GoogleDriveUploadFileFolder.class));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User doesn't want to upload the selected file/folder
                    }
                })
                .create();

        alertDialog.show();
    }
}
