package it.unibs.cloudondemand.google;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.util.Log;

import it.unibs.cloudondemand.LoginActivity;

public class DriveGoogle extends LoginGoogle {
    private Intent data;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        data=getIntent();
    }

    private void saveToDrive() {
        String contentType=data.getStringExtra(LoginActivity.CONTENT_EXTRA);
        String content=data.getStringExtra(LoginActivity.DATA_EXTRA);
        if(contentType==LoginActivity.CONTENT_STRING)
            saveToDriveString(content);
        else if(contentType==LoginActivity.CONTENT_FILE)
            saveToDriveFile(content);
        else if(contentType==LoginActivity.CONTENT_FOLDER)
            saveToDriveFolder(content);
        else
            Log.e("Saving to Drive", "Unable to find the content type");
    }

    private void saveToDriveString(String toSave) {

    }

    private void stringToFile(String string) {

    }

    private void saveToDriveFile(String toSave) {

    }

    private void saveToDriveFolder(String toSave) {

    }
}
