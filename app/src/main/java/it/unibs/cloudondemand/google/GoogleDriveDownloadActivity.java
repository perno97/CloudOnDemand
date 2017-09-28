package it.unibs.cloudondemand.google;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.HashMap;

import it.unibs.cloudondemand.R;

public class GoogleDriveDownloadActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_drive_download);

        HashMap<String, String> itemsList = GoogleDriveUtil.getDatabase(getApplicationContext());
        showList(itemsList);
    }

    private void showList(HashMap<String,String> list){

    }
}
