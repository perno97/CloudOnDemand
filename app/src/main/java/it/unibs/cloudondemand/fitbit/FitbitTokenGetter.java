package it.unibs.cloudondemand.fitbit;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import it.unibs.cloudondemand.R;

public class FitbitTokenGetter extends AppCompatActivity {
    private static final String TAG = "FitbitTokenGetter";
    // Intent to send
    public static final String TOKEN_EXTRA = "fitbitToken";
    // Intent to receive
    public static final String SCOPE_EXTRA = "scope";
    public static final String CALLING_CLASS_EXTRA = "callingClass";

    private static final String AUTHORIZATION_URI = "https://www.fitbit.com/oauth2/authorize";
    private static final String CLIENT_ID = "228L7S";
    private static final String REDIRECT_URI = "it.unibs.cloudondemand://fitbitoauth2callback";


    private String accessToken;
    private String userId;
    private String scope;
    private String tokenType;
    private long expiresIn;

    private String requesterClass;
    private String requestedScope;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        String intentData = intent.getDataString();

        if(intentData != null && intentData.startsWith(REDIRECT_URI)) {
            Log.i(TAG, intentData); //TODO controllare quando viene negato l'aceesso... poi rimuovere
            // Received intent from fitbit server (response)
            handleIntent(intentData.substring(REDIRECT_URI.length()+1));
        }
        else {
            // Activity that has requested for a token
            requesterClass = intent.getStringExtra(CALLING_CLASS_EXTRA);
            requestedScope = intent.getStringExtra(SCOPE_EXTRA);
            onTokenRequest();
        }
    }

    // Initialize attributes and when finish call onIntentReceived
    private void handleIntent(String intentData) {
        int lastIndex = intentData.indexOf('&');
        if(lastIndex == -1)
            lastIndex = intentData.length();
        Log.e(TAG, "intentdata : " + intentData);
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
        else if(intentData.startsWith("state=")) {
            requesterClass = intentData.substring(6, lastIndex);
            handleIntent(intentData.substring(lastIndex+1));
        }
        // Last parameter of Uri
        else if(intentData.startsWith("expires_in=")) {
            expiresIn = Long.parseLong(intentData.substring(11), lastIndex);
            onIntentReceived();
        }
    }

    private void sendBackToken(FitbitToken token) {
        try {
            Intent intent = new Intent(this, Class.forName(requesterClass));
            startActivity(intent);
            // Destroy this activity
            finish();
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "Caller class not found." + e.toString());
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
            sendBackToken(token);
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
                    sendBackToken(inMemoryToken);
                    return;
                }
            }
        }

        // Make request to fitbit server
        String url = AUTHORIZATION_URI + "?response_type=token" + "&client_id="+CLIENT_ID + "&redirect_uri="+REDIRECT_URI + "&scope="+requestedScope + "&state="+requesterClass;
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

    public static Intent getIntent(Context context, String callingClass,String scope) {
        Intent intent = new Intent(context, FitbitTokenGetter.class);
        intent.putExtra(SCOPE_EXTRA, scope);
        intent.putExtra(CALLING_CLASS_EXTRA, callingClass);
        return intent;
    }
}
