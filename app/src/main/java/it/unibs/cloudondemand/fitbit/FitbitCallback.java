package it.unibs.cloudondemand.fitbit;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import it.unibs.cloudondemand.R;

public class FitbitCallback extends AppCompatActivity {
    private static final String TAG = "FitbitCallback";

    private static final String REDIRECT_URI = "it.unibs.cloudondemand://fitbitoauth2callback";

    // EVERY NEW ACTIVITY NEEDED TO BE INSERTED HERE
    private static final String[] FITBIT_CLASSES = {"it.unibs.cloudondemand.fitbit.FitbitAuth",
            "it.unibs.cloudondemand.fitbit.FitbitActivities"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String intentData = getIntent().getDataString();

        handleIntent(intentData.substring(REDIRECT_URI.length()+1));
    }

    private String accessToken;
    private String userId;
    private String scope;
    private String tokenType;
    private long expiresIn;
    private int state;
    private String errorDescription;
    private String error;

    /**
     * Initialize attributes and when finish call onTokenReceived.
     * @param intentData URL with auth response.
     */
    private void handleIntent(String intentData) {
        int lastIndex = intentData.indexOf('&');
        if(lastIndex == -1)
            lastIndex = intentData.length();

        if(intentData.startsWith("access_token=")) {
            accessToken = intentData.substring(13, lastIndex);
        }
        else if(intentData.startsWith("user_id=")) {
            userId = intentData.substring(8, lastIndex);
        }
        else if(intentData.startsWith("scope=")) {
            scope = intentData.substring(6, lastIndex);
        }
        else if(intentData.startsWith("token_type=")) {
            tokenType = intentData.substring(11, lastIndex);
        }
        else if(intentData.startsWith("expires_in=")) {
            expiresIn = Long.parseLong(intentData.substring(11), lastIndex);
        }
        else if(intentData.startsWith("state=")) {
            state = Integer.parseInt(intentData.substring(6, lastIndex));
        }
        // When error occurs
        else if(intentData.startsWith("error_description=")) {
            errorDescription = intentData.substring(18, lastIndex);
        }
        else if(intentData.startsWith("error=")) {
            error = intentData.substring(6, lastIndex);
        }

        // Call onTokenReceived when finished to analyze URl otherwise call recursively this method
        if (intentData.length() == lastIndex)
            onTokenReceived();
        else
            handleIntent(intentData.substring(lastIndex+1));
    }

    /**
     * Called when a token is received.
     */
    private void onTokenReceived() {
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

            // Send back token to requesting activity
            FitbitToken token = new FitbitToken(accessToken, userId, scope, tokenType, expiresIn);
            onTokenAcquired(state);
        }
        else {
            Log.e(TAG, "Token error = " + error + ", description = " + errorDescription);
            onTokenAcquired(state);
        }
    }

    /**
     * Resume requesting activity.
     * @param callingClass Identifier of class to resume.
     */
    private void onTokenAcquired(int callingClass) {
        // Check if is a possible identifier
        if(callingClass == -1) {
            Log.e(TAG, "Class not found. Identifier = -1.");
            Toast.makeText(this, R.string.error_occurred, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Resume calling activity
        try {
            Intent resumeActivity = new Intent(this, Class.forName(FITBIT_CLASSES[callingClass]));
            resumeActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(resumeActivity);
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "Class resolver error.\n" + e.toString());
            Toast.makeText(this, R.string.error_occurred, Toast.LENGTH_SHORT).show();
        }
        // Destroy this activity
        finish();
    }


    /**
     * Used to get the identifier of requesting activity for resuming later when the token is received.
     * @param className Class name (should call this.getClass().getName()).
     * @return
     */
    public static int getClassIdentifier(String className) {
        for(int i = 0; i < FITBIT_CLASSES.length; i++) {
            if(className.equals(FITBIT_CLASSES[i]))
                return i;
        }
        return -1;
    }
}
