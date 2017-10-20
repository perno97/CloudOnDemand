package it.unibs.cloudondemand.dropbox;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.dropbox.core.android.Auth;

import it.unibs.cloudondemand.R;

public class DropboxLoginActivity extends AppCompatActivity {

    private Bundle intentExtras;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        Auth.startOAuth2Authentication(getApplicationContext(), getString(R.string.APP_KEY));
        getAccessToken();

        Intent intent = getIntent();
        intentExtras = intent.getExtras();
    }


    @Override
    protected void onResume() {
        super.onResume();

        getAccessToken();
    }

    public void getAccessToken() {
        String accessToken = Auth.getOAuth2Token(); //generate Access Token

        if (accessToken != null) {
            //Store accessToken in SharedPreferences
            SharedPreferences prefs = getSharedPreferences(getString(R.string.shared_pref_dropbox_account), Context.MODE_PRIVATE);
            prefs.edit().putString(getString(R.string.dropbox_access_token), accessToken).apply();

            //Proceed to MainActivity
            Intent intent = new Intent(this, DropboxMainActivity.class);
            intent.putExtras(intentExtras);
            startActivity(intent);
        }


    }

}
