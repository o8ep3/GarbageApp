package com.example.garbagedayandroidapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "Cognito";
    private SharedPreferences dataStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.login_activity);

        //email, passwordを取得
        final EditText editTextEmail = findViewById(R.id.email);
        final EditText editTextPassword = findViewById(R.id.password);

        final AuthenticationHandler authenticationHandler = new AuthenticationHandler() {
            @Override
            public void onSuccess(CognitoUserSession userSession, CognitoDevice newDevice) {
                Log.i(TAG, "onSuccess: Login successful , can get tokens here");
                Toast.makeText(getApplicationContext(), "サインイン成功", Toast.LENGTH_SHORT).show();


                /*userSession contains the tokens*/

                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                String json = gson.toJson(userSession);
                Log.i(TAG, "user session " + json);

                //ログイン成功した場合
                dataStore = getSharedPreferences("DataStore", MODE_PRIVATE);
                //ログインフラグをtrueに
                SharedPreferences.Editor editor = dataStore.edit();
                editor.putBoolean("LoginFlag", true);
                //mail, passwordを保存
                editor.putString("Email", editTextEmail.getText().toString());
                editor.putString("Password", editTextPassword.getText().toString());
                editor.apply();

                //メイン画面に遷移
                Intent intent = new Intent(getApplication(), MainActivity.class);
                startActivity(intent);
                finish();


            }

            @Override
            public void getAuthenticationDetails(AuthenticationContinuation authenticationContinuation, String userId) {
                Log.i(TAG, "ああああああgetAuthenticationDetails: in getAuthenticationDetails...");
                Log.i(TAG, String.valueOf(editTextEmail.getText()) + String.valueOf(editTextPassword.getText()));

                /*need to get the userId & password to continue*/
                AuthenticationDetails authenticationDetails = new AuthenticationDetails(userId,
                        String.valueOf(editTextPassword.getText()), null);

                //pass the user sign in credentials to the continuation
                authenticationContinuation.setAuthenticationDetails(authenticationDetails);

                //allow the sign-in to continue
                authenticationContinuation.continueTask();

            }

            @Override
            public void getMFACode(MultiFactorAuthenticationContinuation continuation) {
                Log.i(TAG, "getMFACode: ");

            }

            @Override
            public void authenticationChallenge(ChallengeContinuation continuation) {
                Log.i(TAG, "authenticationChallenge: ");

            }

            @Override
            public void onFailure(Exception exception) {
                Log.i(TAG, "onFailure: " + exception.getLocalizedMessage());
                Toast.makeText(getApplicationContext(), "サインイン失敗", Toast.LENGTH_SHORT).show();

            }
        };

        Button returnButton = findViewById(R.id.email_sign_in_button);
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editTextEmail.getText().toString();
                String password = editTextPassword.getText().toString();

                if (email.length() != 0 && password.length() >= 6) {
                    CognitoSettings cognitoSettings = new CognitoSettings(LoginActivity.this);
                    CognitoUser thisUser = cognitoSettings.getUserPool().getUser(email);

                    Log.i(TAG, "onClick: in button clicked");

                    thisUser.getSessionInBackground(authenticationHandler);

                } else {
                    Toast.makeText(getApplicationContext(), "Email, Passwordを正しく入力してください", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
