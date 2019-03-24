package com.example.garbagedayandroidapp;


import android.content.Context;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.regions.Regions;

public class CognitoSettings {

    private String userPoolId = "ap-northeast-1_8xcHUOVG5";
    private String clientId = "6tvoucldvsfdmc02omibbtnluj";
    private String clientSecret = "66q92s868lmnsm8h491sugk2gtbh5ob8tq2eeluimu86ph6520c";
    private Regions cognitoRegion = Regions.AP_NORTHEAST_1;

    private Context context;

    public CognitoSettings(Context context) {
        this.context = context;
    }

    public String getUserPoolId() {
        return userPoolId;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public Regions getCognitoRegion() {
        return cognitoRegion;
    }

    /* the entry point for all interactions with your user pool from your application */
    public CognitoUserPool getUserPool() {
        return new CognitoUserPool(context, userPoolId, clientId,
                clientSecret, cognitoRegion);
    }
}
