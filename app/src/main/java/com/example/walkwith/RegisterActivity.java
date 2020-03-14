package com.example.walkwith;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class RegisterActivity extends AppCompatActivity {

    private EditText firstNameEditText;
    private EditText lastNameEditText;
    private EditText emailEditText;
    private EditText confirmEmailEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private EditText phoneNumberEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firstNameEditText = findViewById(R.id.first_name);
        lastNameEditText = findViewById(R.id.last_name);
        emailEditText = findViewById(R.id.email);
        confirmEmailEditText = findViewById(R.id.confirm_email);
        passwordEditText = findViewById(R.id.password);
        confirmPasswordEditText = findViewById(R.id.confirm_password);
        phoneNumberEditText = findViewById(R.id.phone_number);

        final Button registerButton = findViewById(R.id.register);
        final Button gotAccountButton = findViewById(R.id.gotAccount);

        registerButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                validateInfo();
            }
        });

        gotAccountButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void validateInfo() {
        // Check for stuff like emails with @ etc.
        if (emailEditText.getText().toString().equals(confirmEmailEditText.getText().toString()))
            // Emails same
            if (passwordEditText.getText().toString().equals(
                    confirmPasswordEditText.getText().toString()))
                // Passwords same
                sendRegisterRequest(firstNameEditText.getText().toString(), lastNameEditText.getText().toString(), emailEditText.getText().toString(),
                passwordEditText.getText().toString(), phoneNumberEditText.getText().toString());
            else
                Toast.makeText(this, "Passwords don't match", Toast.LENGTH_SHORT)
                        .show();
        else
            Toast.makeText(this, "Emails don't match", Toast.LENGTH_SHORT).show();

    }

    private void sendRegisterRequest(String firstName, String lastName, String email, String password, String phone) {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = getResources().getString(R.string.server_ip) + "account";


        try {
            JSONObject jsonBody = new JSONObject();

            jsonBody.put("mode", "register");
            jsonBody.put("firstName", firstName);
            jsonBody.put("lastName", lastName);
            jsonBody.put("email", email);
            jsonBody.put("password", password);
            jsonBody.put("phoneNum", phone);
            // Put your headers here

            JsonObjectRequest jsonObject = new JsonObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        validateLogin((String) response.get("result"));
                        // Put the things you want to happen upon success
                    } catch (JSONException e) {
                        validateLogin("JSON Error - " + e.getMessage());
                        // Put the error here
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // put the error here
                    validateLogin("Server Error - " + error.getMessage());
                }
            });
            // Add the request to the RequestQueue.
            queue.add(jsonObject);

        } catch (
                JSONException e) {
            validateLogin("Error - " + e.getMessage());
        }
    }

    private void validateLogin(String response) {
        if (response.equals("True")) {
            Toast.makeText(this, "Welcome, " + emailEditText.getText().toString(), Toast.LENGTH_SHORT).show();
            Intent myIntent = new Intent(RegisterActivity.this, MainMenu.class);
            finish();
            RegisterActivity.this.startActivity(myIntent);
        }
        else
            Toast.makeText(this, response, Toast.LENGTH_SHORT).show();

    }

}
