package it.unibs.cloudondemand.fitbit;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.Iterator;

import it.unibs.cloudondemand.R;

public class FitbitActivities extends FitbitConnection {
    private static final String TAG = "FitbitActivities";

    private static final String SCOPE = "profile activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fitbit_activities);
    }

    @Override
    public void onTokenAcquired(FitbitToken token) {
        if(token == null) {
            Toast.makeText(this, getString(R.string.unable_connect_account), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String url = "https://api.fitbit.com/1/user/" + token.getUserId() + "/activities/list.json?sort=desc&offset=0&limit=20";
        makeAPIRequestGet(token, url, activitiesResponse);
    }

    private OnAPIResponse activitiesResponse = new OnAPIResponse() {
        @Override
        public void onResponse(String headerResponse, JSONObject response, URL urlRequest) {
            if(response == null) {
                Toast.makeText(FitbitActivities.this, R.string.error_occurred, Toast.LENGTH_SHORT).show();
                return;
            }

            TextView mTextView = (TextView) findViewById(R.id.fitbit_response_text);
            /*
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
            }*/
            mTextView.setText(response.toString());
        }
    };

    /**
     * Util method to retrieve intent to launch this activity.
     * @param context Context of activity that launch the intent.
     * @return Intent to launch with startActivity(intent).
     */
    public static Intent getIntent(Context context) {
        return new Intent(context, FitbitActivities.class);
    }

    @Override
    public String getScopes() {
        return SCOPE;
    }
}
