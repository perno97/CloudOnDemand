package it.unibs.cloudondemand.dropbox;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.dropbox.core.android.Auth;

import it.unibs.cloudondemand.R;

public class DropboxLoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();

        getAccessToken();
        Auth.startOAuth2Authentication(getApplicationContext(), getString(R.string.APP_KEY));
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    public void getAccessToken() {
        String accessToken = Auth.getOAuth2Token(); //generate Access Token
        if (accessToken != null) {
            //Store accessToken in SharedPreferences
            SharedPreferences prefs = getSharedPreferences(".dropbox", Context.MODE_PRIVATE);
            prefs.edit().putString("access-token", accessToken).apply();

            //Proceed to MainActivity
            Intent intent = new Intent(DropboxLoginActivity.this, DropboxMainActivity.class);
            startActivity(intent);
        }
    }
}