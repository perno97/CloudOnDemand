package it.unibs.cloudondemand.fitbit;

import android.os.Parcel;
import android.os.Parcelable;


class FitbitToken {
    private String accessToken;
    private String userId;
    private String scope;
    private String tokenType;
    private long expiresIn;

    public FitbitToken(String accessToken, String userId, String scope, String tokenType, long expiresIn) {
        this.accessToken = accessToken;
        this.userId = userId;
        this.scope = scope;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getUserId() {
        return userId;
    }

    public String getScope() {
        return scope;
    }

    public String getTokenType() {
        return tokenType;
    }

    public long getExpiresIn() {
        return expiresIn;
    }
}
