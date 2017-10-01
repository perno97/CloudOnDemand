package it.unibs.cloudondemand.fitbit;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import it.unibs.cloudondemand.R;

public abstract class FitbitConnection extends AppCompatActivity {
    private static final String TAG = "FitbitConnection";

    private static final String AUTHORIZATION_URI = "https://www.fitbit.com/oauth2/authorize";
    private static final String CLIENT_ID = "228L7S";
    private static final String REDIRECT_URI = "it.unibs.cloudondemand://fitbitoauth2callback";


    private String accessToken;
    private String userId;
    private String scope;
    private String tokenType;
    private long expiresIn;
    private String errorDescription;

    private String requestedScope;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        String intentData = intent.getDataString();

        if(intentData != null && intentData.startsWith(REDIRECT_URI)) {
            // Received intent from fitbit server (response)
            handleIntent(intentData.substring(REDIRECT_URI.length()+1));
        }
        else {
            requestedScope = getScopes();
            onTokenRequest();
        }
    }

    // Initialize attributes and when finish call onIntentReceived
    private void handleIntent(String intentData) {
        int lastIndex = intentData.indexOf('&');
        if(lastIndex == -1)
            lastIndex = intentData.length();

        if(intentData.startsWith("access_token=")) {
            accessToken = intentData.substring(13, lastIndex);
            handleIntent(intentData.substring(lastIndex+1));
        }
        else if(intentData.startsWith("user_id=")) {
            userId = intentData.substring(8, lastIndex);
            handleIntent(intentData.substring(lastIndex+1));
        }
        else if(intentData.startsWith("scope=")) {
            scope = intentData.substring(6, lastIndex);
            handleIntent(intentData.substring(lastIndex+1));
        }
        else if(intentData.startsWith("token_type=")) {
            tokenType = intentData.substring(11, lastIndex);
            handleIntent(intentData.substring(lastIndex+1));
        }
        // Last parameter of Uri
        else if(intentData.startsWith("expires_in=")) {
            expiresIn = Long.parseLong(intentData.substring(11), lastIndex);
            onIntentReceived();
        }
        // When error occurred
        else if(intentData.startsWith("error_description=")) {
            errorDescription = intentData.substring(18, lastIndex);
            onIntentReceived();
        }
    }

    private void onIntentReceived() {
        long expirationDate = System.currentTimeMillis() + expiresIn;
        // Save token to shared preferences
        if(accessToken != null) {
            SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.shared_pref_fitbit_account), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(getString(R.string.fitbit_access_token), accessToken);
            editor.putString(getString(R.string.fitbit_user_id), userId);
            editor.putString(getString(R.string.fitbit_scope), scope);
            editor.putString(getString(R.string.fitbit_token_type), tokenType);
            editor.putLong(getString(R.string.fitbit_expiration_date), expirationDate);
            editor.apply();

            // Send back using intent
            FitbitToken token = new FitbitToken(accessToken, userId, scope, tokenType, expiresIn);
            onTokenAcquired(token);
        }
        else {
            Log.e(TAG, "Token error description = " + errorDescription);
            onTokenAcquired(null);
        }
    }

    private void onTokenRequest() {
        // Retrieve token from shared preferences
        FitbitToken inMemoryToken = getTokenFromSharedPref();
        if(inMemoryToken != null) {
            // Check if token in memory is expired
            if(inMemoryToken.getExpiresIn() > 0) {

                // Check if token in memory has scope
                boolean hasScope = true;
                for(String currentScope : requestedScope.split(" ")) {
                    if(!inMemoryToken.getScope().contains(currentScope)) {
                        hasScope = false;
                        break;
                    }
                }
                if(hasScope) {
                    // Send back in memory token using intent
                    onTokenAcquired(inMemoryToken);
                    return;
                }
            }
        }

        // Make request to fitbit server
        String url = AUTHORIZATION_URI + "?response_type=token" + "&client_id="+CLIENT_ID + "&redirect_uri="+REDIRECT_URI + "&scope="+requestedScope;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
        // Destroy this activity
        finish();
    }

    // Read token from shared preferences
    private FitbitToken getTokenFromSharedPref() {
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.shared_pref_fitbit_account), Context.MODE_PRIVATE);
        accessToken = sharedPreferences.getString(getString(R.string.fitbit_access_token), null);
        userId = sharedPreferences.getString(getString(R.string.fitbit_user_id), null);
        scope = sharedPreferences.getString(getString(R.string.fitbit_scope), null);
        tokenType = sharedPreferences.getString(getString(R.string.fitbit_token_type), null);
        long expirationDate = sharedPreferences.getLong(getString(R.string.fitbit_expiration_date), 0);
        expiresIn = expirationDate - System.currentTimeMillis();

        if(accessToken != null)
            return new FitbitToken(accessToken, userId, scope, tokenType, expiresIn);
        else
            return null;
    }

    /**
     * Entry point for subclasses. Successfully requested token.
     * @param token Token acquired.
     */
    public abstract void onTokenAcquired(FitbitToken token);

    /**
     * Getter requested scope from activity.
     * @return Requested scopes separated by ' '.
     */
    public abstract String getScopes();
}
