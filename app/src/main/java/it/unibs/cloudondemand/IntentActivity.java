package it.unibs.cloudondemand;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.File;

public class IntentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        Uri uri = intent.getData();

        File file = new File(uri.getPath());

        int contentType;
        if(file.isDirectory())
            contentType = LoginActivity.CONTENT_FOLDER;
        else
            contentType = LoginActivity.CONTENT_FILE;

        String content = file.getPath();

        Intent toLaunch = LoginActivity.getIntent(this, contentType, content);
        startActivity(toLaunch);

        finish();
    }
}
