package it.unibs.cloudondemand.fitbit;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import it.unibs.cloudondemand.R;

public class FitbitAuth extends AppCompatActivity {
    private static final String TAG = "FitbitAuth";

    private static final String AUTHORIZATION_URI = "https://www.fitbit.com/oauth2/authorize";
    private static final String CLIENT_ID = "228L7S";
    private static final String REDIRECT_URI = "it.unibs.cloudondemand://fitbitoauth2callback";
    private static final String SCOPE = "profile";

    private boolean handledIntent = false;

    private String accessToken;
    private String userId;
    private String scope;
    private String tokenType;
    private long expiresIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fitbit_auth);

        Intent intent = getIntent();

        String intentData = intent.getDataString();
        if(intentData.startsWith(REDIRECT_URI))
            handleIntent(intentData.substring(REDIRECT_URI.length()+1));
    }

    // Initialize attributes and when finish call onIntentRead
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
            onIntentRead();
        }
    }

    // Called when the intent is handled, make API request
    private void onIntentRead() {
        handledIntent = true;

        URL url = null;
        try {
            url = new URL("https://api.fitbit.com/1/user/" + userId + "/profile.json");
        } catch (MalformedURLException e) {
            // Should never get here
            Log.e(TAG, "Error while creating URL object." + e+toString());
        }
        makeAPIRequest.execute(url);
    }


    private AsyncTask<URL, Void, JSONObject> makeAPIRequest = new AsyncTask<URL, Void, JSONObject>() {
        @Override
        protected JSONObject doInBackground(URL... params) {

            HttpURLConnection urlConnection = null;
            JSONObject read = null;
            try {
                // Create HTTP connection
                urlConnection = (HttpURLConnection) params[0].openConnection();
                // Set HTTP header
                urlConnection.setRequestMethod("GET");
                urlConnection.setRequestProperty("Authorization", tokenType + " " + accessToken);

                // Read string from response
                BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                // Create JSONObject from read string
                read = new JSONObject(in.readLine());
            }
            catch (Exception e) {
                Log.e(TAG, "Exception while connecting to Fitbit server." + e.toString());
            }
            finally {
                if(urlConnection != null)
                    urlConnection.disconnect();
            }

            if(read != null)
                return read;
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            handleJsonResponse(jsonObject);
        }
    };

    // Show user json object into text view
    private void handleJsonResponse(JSONObject jsonObject) {
        TextView mTextView = (TextView) findViewById(R.id.fitbit_response_text);
        mTextView.setText("");
        try {
            JSONObject user = jsonObject.getJSONObject("user");
            Iterator<String> keys = user.keys();
            String key = keys.next();
            while (keys.hasNext()) {
                mTextView.append(key + " : " + user.getString(key) + "\n");
                key = keys.next();
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error occurred while reading user json object. " + e.toString());
            mTextView.setText("Error occurred while reading Fitbit response");
        }
    }


    /**
     * Util method to retrieve intent to launch for authentication.
     * @param context Context of activity that launch the intent.
     * @return Intent to launch with startActivity(intent).
     */
    public static Intent getIntent(Context context) {
        //TODO Before check if token is valid

        // Open browser for request permission
        String url = AUTHORIZATION_URI + "?response_type=token" + "&client_id="+CLIENT_ID + "&redirect_uri="+REDIRECT_URI + "&scope="+SCOPE;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        return intent;
    }
}
