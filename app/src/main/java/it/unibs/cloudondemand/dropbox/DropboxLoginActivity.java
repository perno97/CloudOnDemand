package it.unibs.cloudondemand.dropbox;

/**
 * Activity per impostare il login tramite Dropbox
 */


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.dropbox.core.android.Auth;

import it.unibs.cloudondemand.R;

public class DropboxLoginActivity extends AppCompatActivity {

    private Bundle intentExtras;

    /**
     * Autenticazione utente e richiesta token
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Auth.startOAuth2Authentication(getApplicationContext(), getString(R.string.APP_KEY));
        getAccessToken();

        Intent intent = getIntent();
        intentExtras = intent.getExtras();
    }

    /**
     * Richiesta token
     */
    @Override
    protected void onResume() {
        super.onResume();

        getAccessToken();
    }

    /**
     * Permette di iniziare la procedura tramite la quale verrà ottenuto il token necessario
     * per l'autenticazione
     */
    public void getAccessToken() {
        String accessToken = Auth.getOAuth2Token(); //genera Access Token

        if (accessToken != null) {
            //Salva accessToken in SharedPreferences
            SharedPreferences prefs = getSharedPreferences(getString(R.string.shared_pref_dropbox_account), Context.MODE_PRIVATE);
            prefs.edit().putString(getString(R.string.dropbox_access_token), accessToken).apply();

            //Procede alla MainActivity
            Intent intent = new Intent(this, DropboxMainActivity.class);
            intent.putExtras(intentExtras);
            startActivity(intent);
        }


    }

}
