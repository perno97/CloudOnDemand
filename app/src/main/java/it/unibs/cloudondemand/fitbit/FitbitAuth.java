package it.unibs.cloudondemand.fitbit;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.Iterator;

import it.unibs.cloudondemand.R;

public class FitbitAuth extends FitbitConnection {
    private static final String TAG = "FitbitAuth";

    private static final String SCOPE = "profile";


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

        // Make API request
        makeAPIRequestGet(token, "https://api.fitbit.com/1/user/" + token.getUserId() + "/profile.json", profileResponse);
    }

    /**
     * Handle response of profile data request.
     */
    private OnAPIResponse profileResponse = new OnAPIResponse() {
        @Override
        public void onResponse(String headerResponse, JSONObject response, URL urlRequest) {
            // Check if is a valid response
            if (headerResponse == null || response == null) {
                Toast.makeText(FitbitAuth.this, "Error occurred while calling fitbit server.", Toast.LENGTH_SHORT).show();  //TODO res/strings
                return;
            }

            TextView mTextView = (TextView) findViewById(R.id.fitbit_response_text);
            mTextView.setText("");
            try {
                JSONObject user = response.getJSONObject("user");
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
    };

    /**
     * Util method to retrieve intent to launch this activity.
     * @param context Context of activity that launch the intent.
     * @return Intent to launch with startActivity(intent).
     */
    public static Intent getIntent(Context context) {
        return new Intent(context, FitbitAuth.class);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fitbit_button_activity2:
                startActivity(FitbitActivities.getIntent(this));
                break;
        }
    }

    @Override
    public String getScopes() {
        return SCOPE;
    }

    @Override
    public String getClassName() {
        return this.getClass().getName();
    }
}
