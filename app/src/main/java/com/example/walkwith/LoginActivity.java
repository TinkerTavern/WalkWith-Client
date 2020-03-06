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

public class LoginActivity extends AppCompatActivity {

    private EditText usernameEditText;

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
            public void onClick(View v) {
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
                sendAccountPOST("changePass", usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        });
    }

    private void sendAccountPOST(String mode, String email, String password) {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String ip = "138.38.194.92"; // Replace this with your own
        String port = "5000"; // Usually this
        String url = "http://" + ip + ":" + port + "/account";

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
        if (response.equals("True")) {
            Toast.makeText(this, "Welcome, " + usernameEditText.getText().toString(), Toast.LENGTH_SHORT).show();
            Intent myIntent = new Intent(LoginActivity.this, MainMenu.class);
            finish();
            LoginActivity.this.startActivity(myIntent);
        }
        else
            Toast.makeText(this, response, Toast.LENGTH_SHORT).show();

    }

}
