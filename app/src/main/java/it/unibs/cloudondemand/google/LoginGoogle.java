package it.unibs.cloudondemand.google;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;

import it.unibs.cloudondemand.LoginInterface;
import it.unibs.cloudondemand.R;

public class LoginGoogle extends AppCompatActivity {

        Context context;
        GoogleApiClient mGoogleApiClient;
        private static final int RC_SIGN_IN = 1;

        public Login(Context context){
            this.context=context;
        }
        @Override
        public void doLogin() {
            if(isLogged()) return;

            // Configure sign-in to request the user's ID, email address, and basic
            // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build();

            // Build a GoogleApiClient with access to the Google Sign-In API and the
            // options specified by gso.
            mGoogleApiClient = new GoogleApiClient.Builder(context)
                    .enableAutoManage((FragmentActivity) context /* FragmentActivity */, (GoogleApiClient.OnConnectionFailedListener)context /* OnConnectionFailedListener */)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .build();
        }

        public void onActivityResult(int requestCode, int resultCode, Intent data) {

            // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
            if (requestCode == RC_SIGN_IN) {
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                Toast.makeText(context, "funziona", Toast.LENGTH_SHORT).show();
            }
        }

        private void signIn() {
            Activity a = (Activity)context;
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
            a.startActivityForResult(signInIntent, RC_SIGN_IN);
        }

        @Override
        public boolean isLogged() {
            return false;
        }

        @Override
        public void doLogout() {

        }
    }
