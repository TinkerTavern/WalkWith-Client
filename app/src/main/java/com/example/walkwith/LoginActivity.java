package com.example.walkwith;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.walkwith.utils.Utilities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameEditText;
    ArrayList<String> friendsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameEditText = findViewById(R.id.email);
        final EditText passwordEditText = findViewById(R.id.password);
        final Button loginButton = findViewById(R.id.login);
        final Button createAccountButton = findViewById(R.id.createAccount);
        final Button forgotPasswordButton = findViewById(R.id.forgotPassword);
        final Switch rememberMe = findViewById(R.id.rememberMe);
        SharedPreferences preferences = getSharedPreferences("checkbox", MODE_PRIVATE);
        String theme = preferences.getString("theme","");
        if (!theme.equals(""))
            AppCompatDelegate.setDefaultNightMode(Integer.parseInt(theme));
        String isRemember = preferences.getString("remember","");
        if (isRemember.equals("true")) {
            String email = preferences.getString("email","");
            if (!email.equals(""))
                successfulLogin(email);
        }

        rememberMe.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = preferences.edit();
            if (buttonView.isChecked()) {
                editor.putString("remember", "true");
                Toast.makeText(getApplicationContext(), "Login details will be remembered",
                        Toast.LENGTH_SHORT).show();
            } else {
                editor.putString("remember", "false");
                Toast.makeText(getApplicationContext(), "Will not remember login details",
                        Toast.LENGTH_SHORT).show();
            }
            editor.apply();
        });

        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    sendAccountPOST("login", usernameEditText.getText().toString(),
                            passwordEditText.getText().toString());
                    return true;
                }
                return false;
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { // TODO: (Re)implement loading icon for screen
                sendAccountPOST("login", usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        });

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent myIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                LoginActivity.this.startActivity(myIntent);
            }
        });

        forgotPasswordButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { // TODO: Do forgot passwords correctly
                Intent myIntent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                LoginActivity.this.startActivity(myIntent);
            }
        });
    }

    private void sendAccountPOST(String mode, final String email, String password) {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = getResources().getString(R.string.server_ip) + "account";
        try {
            JSONObject jsonBody = new JSONObject();

             jsonBody.put("mode", mode);
             jsonBody.put("email", email);
             jsonBody.put("password", password);
            // Put your headers here

            JsonObjectRequest jsonObject = new JsonObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        validateLogin((String) response.get("result"));
                        // Put the things you want to happen upon success
                    } catch (JSONException e) {
                         validateLogin(e.getMessage());
                        // Put the error here
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // put the error here
                     validateLogin(error.getMessage());
                }
            });
            // Add the request to the RequestQueue.
            queue.add(jsonObject);

        } catch (
                JSONException e) {
            e.printStackTrace();
        }
    }

    private void validateLogin(String response) {
        String email = usernameEditText.getText().toString();
        if (response.equals("True")) {
            Toast.makeText(this, "Welcome, " + email,
                    Toast.LENGTH_SHORT).show();
            SharedPreferences preferences = getSharedPreferences("checkbox", MODE_PRIVATE);
            String isRemembered = preferences.getString("remember", "");

            if (isRemembered.equals("true")) { // So to save login details for the future
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("email", email);
                editor.apply();
            }
            successfulLogin(email);
        }
        else
            Toast.makeText(this, response, Toast.LENGTH_SHORT).show();
    }

    private void successfulLogin(String email) {
        Intent myIntent = new Intent(LoginActivity.this, MainMenu.class);
        // TODO: Pass in user information into screen
        getUserInfo(email);
        finish();
        LoginActivity.this.startActivity(myIntent);
    }

    private void getUserInfo(String email) {
        new AccountInfo(email);
        Utilities.updateTrustedContacts(this);
    }


}
