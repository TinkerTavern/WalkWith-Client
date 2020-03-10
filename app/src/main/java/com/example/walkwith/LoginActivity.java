package com.example.walkwith;

import android.content.Intent;
import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
                        validateLogin((String) response.get("result"), email);
                        // Put the things you want to happen upon success
                    } catch (JSONException e) {
                         validateLogin(e.getMessage(), email);
                        // Put the error here
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // put the error here
                     validateLogin(error.getMessage(), email);
                }
            });
            // Add the request to the RequestQueue.
            queue.add(jsonObject);

        } catch (
                JSONException e) {
            e.printStackTrace();
        }
    }

    private void validateLogin(String response, String email) {
        if (response.equals("True")) {
            Toast.makeText(this, "Welcome, " + usernameEditText.getText().toString(),
                    Toast.LENGTH_SHORT).show();
            Intent myIntent = new Intent(LoginActivity.this, MainMenu.class);
            // TODO: Pass in user information into screen
            getUserInfo(email);
            finish();
            LoginActivity.this.startActivity(myIntent);
        }
        else
            Toast.makeText(this, response, Toast.LENGTH_SHORT).show();
    }

    private void getUserInfo(String email) {
        getWatchEmailPOST("Account Info", email);
        new AccountInfo(email, friendsList);
    }

    private void getWatchEmailPOST(String mode, String email) {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = getResources().getString(R.string.server_ip) + "account";
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("email", email);
            jsonBody.put("viewingMode", mode);

            JsonObjectRequest jsonObject = new JsonObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        convertToList((String[]) response.get("watchEmails"));
                    } catch (JSONException e) {
                        e.getMessage();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.getMessage();
                }
            });
            queue.add(jsonObject);
        } catch (
                JSONException e) {
            e.printStackTrace();
        }
    }

    private void convertToList(String[] watchEmails) {
        friendsList = new ArrayList<String>(Arrays.asList(watchEmails));
    }
}
