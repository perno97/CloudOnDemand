package it.unibs.cloudondemand.fitbit;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import it.unibs.cloudondemand.R;

public class FitbitAuth extends FitbitConnection {
    private static final String TAG = "FitbitAuth";

    private static final String SCOPE = "profile";

    private FitbitToken token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fitbit_auth);
    }

    @Override
    public void onTokenAcquired(FitbitToken token) {
        if(token == null) {
            Toast.makeText(this, getString(R.string.unable_connect_account), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        this.token = token;

        // Make API request
        URL url = null;
        try {
            url = new URL("https://api.fitbit.com/1/user/" + token.getUserId() + "/profile.json");
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
                urlConnection.setRequestProperty("Authorization", token.getTokenType() + " " + token.getAccessToken());

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

            return read;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            if(jsonObject != null)
                handleJsonResponse(jsonObject);
            else {
                Log.e(TAG, "Empty response from fitbit server.");
                Toast.makeText(FitbitAuth.this, "An error occurred while trying to connect to fitbit server.", Toast.LENGTH_SHORT).show();
            }
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
            mTextView.setText(R.string.unable_connect_account);
        }
    }

    /**
     * Util method to retrieve intent to launch for upload.
     * @param context Context of activity that launch the intent.
     * @return Intent to launch with startActivity(intent).
     */
    public static Intent getIntent(Context context) {
        return new Intent(context, FitbitAuth.class);
    }

    @Override
    public String getScopes() {
        return SCOPE;
    }
}
