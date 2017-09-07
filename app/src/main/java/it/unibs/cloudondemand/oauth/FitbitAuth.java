package it.unibs.cloudondemand.oauth;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

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
            expiresIn = Long.parseLong(intentData.substring(11));
            onIntentRead();
        }
    }

    // Called when the intent is handled, make API request
    private void onIntentRead() {
        handledIntent = true;

        makeAPIRequest.execute();
    }


    private AsyncTask<Void, Void, JSONObject> makeAPIRequest = new AsyncTask<Void, Void, JSONObject>() {
        @Override
        protected JSONObject doInBackground(Void... params) {

            HttpURLConnection urlConnection = null;
            JSONObject read = null;
            try {
                URL url = new URL("https://api.fitbit.com/1/user/" + userId + "/profile.json");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setRequestProperty("Authorization", tokenType + " " + accessToken);


                BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                read = new JSONObject(in.readLine());
            }
            catch (Exception e) {
                Log.e(TAG, e.toString());
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
            TextView mTextView = (TextView) findViewById(R.id.fitbit_response_text);
            mTextView.setText(jsonObject.toString());
        }
    };


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
