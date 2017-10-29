package it.unibs.cloudondemand.fitbit;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import it.unibs.cloudondemand.R;

public abstract class FitbitConnection extends AppCompatActivity {
    private static final String TAG = "FitbitConnection";

    private static final String AUTHORIZATION_URI = "https://www.fitbit.com/oauth2/authorize";
    private static final String CLIENT_ID = "228L7S";
    private static final String REDIRECT_URI = "it.unibs.cloudondemand://fitbitoauth2callback";

    // Scope requested from subclass
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

    private String accessToken;
    private String userId;
    private String scope;
    private String tokenType;
    private long expiresIn;
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
            onTokenAcquired(token);
        }
        else {
            Log.e(TAG, "Token error = " + error + ", description = " + errorDescription);
            onTokenAcquired(null);
        }
    }

    /**
     * Search for a token in memory, if not found make request to server.
     */
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
                    // Send back in memory token to requesting activity
                    onTokenAcquired(inMemoryToken);
                    return;
                }
            }
        }
        // There isn't a token in shared preferences

        // Make request to fitbit server
        String url = AUTHORIZATION_URI + "?response_type=token" + "&client_id="+CLIENT_ID + "&redirect_uri="+REDIRECT_URI + "&scope="+requestedScope;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
        // Destroy this activity
        finish();
    }


    /**
     * Read token from shared preferences.
     * @return Token read, otherwise null.
     */
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
     * Called by subclasses that want to make an API request with GET protocol.
     * @param token Alive token.
     * @param url Request URL.
     * @param apiResponse Callback when response is ready.
     */
    public void makeAPIRequestGet(FitbitToken token, String url, OnAPIResponse apiResponse) {
        new MakeGetRequestAsync().execute(token, url, apiResponse);
    }

    /**
     * Make a GET API request in async task
     */
    private static class MakeGetRequestAsync extends AsyncTask<Object, Void, Pair<String, JSONObject>> {
        private URL urlRequest;
        private OnAPIResponse apiResponse;

        // params[0] = FitbitToken, params[1] = String(URL), params[2] = OnAPIResponse(Callback)
        @Override
        protected Pair<String, JSONObject> doInBackground(Object... params) {
            FitbitToken token = (FitbitToken) params[0];
            String url = (String) params[1];
            apiResponse = (OnAPIResponse) params[2];

            HttpsURLConnection urlConnection = null;
            JSONObject read = null;
            String headerResponse = null;
            try {
                urlRequest = new URL(url);
                // Create HTTP connection
                urlConnection = (HttpsURLConnection) urlRequest.openConnection();
                // Set HTTP header
                urlConnection.setRequestMethod("GET");
                urlConnection.setRequestProperty("Authorization", token.getTokenType() + " " + token.getAccessToken());

                    headerResponse = urlConnection.getHeaderField(null);

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

            return new Pair<>(headerResponse, read);
        }

        @Override
        protected void onPostExecute(Pair<String, JSONObject> response) {
            // Log error
            if(response.first != null)
                Log.e(TAG, "Response(GET) HTTP header : " + response.first);
            else
                Log.e(TAG, "Response(GET) null (also header).");

            // Make callback
            apiResponse.onResponse(response.first, response.second, urlRequest);
        }
    }


    /**
     * Called by subclasses that want to make an API request with POST protocol.
     * @param token Alive token.
     * @param url Request URL.
     * @param postContent Parameters to put in post.
     * @param apiResponse Callback when response is ready.
     */
    public void makeAPIRequestPost(FitbitToken token, String url, Map<String, String> postContent,OnAPIResponse apiResponse) {
        new MakePostRequestAsync().execute(token, url, postContent, apiResponse);
    }

    /**
     * Make a GET API request in async task
     */
    private static class MakePostRequestAsync extends AsyncTask<Object, Void, Pair<String, JSONObject>> {
        private URL urlRequest;
        private OnAPIResponse apiResponse;

        // params[0] = FitbitToken, params[1] = String(URL), params[2] = Map<String,String>(postContent), params[3] = OnAPIResponse(Callback)
        @Override
        protected Pair<String, JSONObject> doInBackground(Object... params) {
            FitbitToken token = (FitbitToken) params[0];
            String url = (String) params[1];
            Map<String,String> postContent = (Map<String, String>) params[2];
            apiResponse = (OnAPIResponse) params[3];

            HttpsURLConnection urlConnection = null;
            JSONObject read = null;
            String headerResponse = null;
            try {
                urlRequest = new URL(url);
                // Create HTTP connection
                urlConnection = (HttpsURLConnection) urlRequest.openConnection();
                // Set HTTP header
                urlConnection.setRequestProperty("Authorization", token.getTokenType() + " " + token.getAccessToken());
                // Method POST without known body length
                urlConnection.setDoOutput(true);
                urlConnection.setChunkedStreamingMode(0);

                // Post content
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream()));
                // Build string to send
                boolean first = true;
                StringBuilder stringBuilder = new StringBuilder();
                for(String key : postContent.keySet()) {
                    if(first)
                        first = false;
                    else
                        stringBuilder.append("&");
                    stringBuilder.append(key);
                    stringBuilder.append("=");
                    stringBuilder.append(postContent.get(key));
                }
                out.write(stringBuilder.toString());

                headerResponse = urlConnection.getHeaderField(null);

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

            return new Pair<>(headerResponse, read);
        }

        @Override
        protected void onPostExecute(Pair<String, JSONObject> response) {
            // Log error
            if(response.first != null)
                Log.e(TAG, "Response(GET) HTTP header : " + response.first);
            else
                Log.e(TAG, "Response(GET) null (also header).");

            // Make callback
            apiResponse.onResponse(response.first, response.second, urlRequest);
        }
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


    /**
     * Callback for API response.
     */
    public interface OnAPIResponse {
        /**
         * Called when is received a response for an API request.
         * @param response Response.
         * @param urlRequest Url used for the request.
         */
        void onResponse(String headerResponse, JSONObject response, URL urlRequest);
    }
}
