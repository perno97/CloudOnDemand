package it.unibs.cloudondemand.fitbit;

import android.os.Parcel;
import android.os.Parcelable;


class FitbitToken implements Parcelable {
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

    public static final Parcelable.Creator<FitbitToken> CREATOR
            = new Parcelable.Creator<FitbitToken>() {
        public FitbitToken createFromParcel(Parcel in) {
            return new FitbitToken(in);
        }

        public FitbitToken[] newArray(int size) {
            return new FitbitToken[size];
        }
    };

    private FitbitToken(Parcel in) {
        accessToken = in.readString();
        userId = in.readString();
        scope = in.readString();
        tokenType = in.readString();
        expiresIn = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(accessToken);
        dest.writeString(userId);
        dest.writeString(scope);
        dest.writeString(tokenType);
        dest.writeLong(expiresIn);
    }

    @Override
    public int describeContents() {
        return 0;
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
