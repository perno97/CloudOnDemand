package it.unibs.cloudondemand.fitbit;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

    private static final String AUTHORIZATION_URI = "https://www.fitbit.com/oauth2/authorize";
    private static final String CLIENT_ID = "228L7S";
    private static final String REDIRECT_URI = "it.unibs.cloudondemand://fitbitoauth2callback";
    //private static final String SCOPE = "profile";

    private static String requesterClass2;

    private String accessToken;
    private String userId;
    private String scope;
    private String tokenType;
    private long expiresIn;
    private String requesterClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        String intentData = intent.getDataString();
        if(intentData.startsWith(REDIRECT_URI)) {
            // Received intent from fitbit server (response)
            handleIntent(intentData.substring(REDIRECT_URI.length()+1));
        }
        else {
            // Activity want token
            requesterClass2 = intent.getPackage();
            Log.e("AFHABFADHFBAHF", "package:" + requesterClass2);
            scope = intent.getStringExtra(SCOPE_EXTRA);
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
        else if(intentData.startsWith("expires_in=")) {
            expiresIn = Long.parseLong(intentData.substring(11), lastIndex);
            handleIntent(intentData.substring(lastIndex+1));
        }
        else if(intentData.startsWith("state=")) {
            requesterClass = intentData.substring(6, lastIndex);
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
        }

        // Send back using intent
        FitbitToken token = new FitbitToken(accessToken, userId, scope, tokenType, expiresIn);
        try {
            Intent intent = new Intent(this, Class.forName(requesterClass));
            if(accessToken != null)
                intent.putExtra(TOKEN_EXTRA, token);
            Toast.makeText(this, "Staring activity", Toast.LENGTH_SHORT).show();
            startActivity(intent);
            // Destroy this activity
            finish();
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "Caller class not found." + e.toString());
        }
    }

    private void onTokenRequest() {
        // check if token in memory is expired
        // check if token in memory has scope
        // send back token in memory using intent
        // make request to fitbit server
        // Destroy this activity
    }
}
