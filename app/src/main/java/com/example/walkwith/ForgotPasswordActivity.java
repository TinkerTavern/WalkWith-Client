package com.example.walkwith;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText emailEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        final Button changePasswordButton = findViewById(R.id.change_password);
        final EditText passwordEditText = findViewById(R.id.password);
        emailEditText = findViewById(R.id.email);

        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { // TODO: Do forgot passwords correctly
                sendAccountPOST(emailEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        });
    }

    private void sendAccountPOST(String email, String password) {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = getResources().getString(R.string.server_ip) + "account";
        try {
            JSONObject jsonBody = new JSONObject();

            jsonBody.put("mode", "changePass");
            jsonBody.put("email", email);
            jsonBody.put("password", password);

            JsonObjectRequest jsonObject = new JsonObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        handleResponse((String) response.get("result"));
                    } catch (JSONException e) {
                        handleResponse(e.getMessage());
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    handleResponse(error.getMessage());
                }
            });
            queue.add(jsonObject);

        } catch (
                JSONException e) {
            e.printStackTrace();
        }
    }

    private void handleResponse(String result) {
        if (result.equals("True")) {
            Toast.makeText(this, emailEditText.getText().toString() + ", your password has been changed",
                    Toast.LENGTH_SHORT).show();
            Intent myIntent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
            ForgotPasswordActivity.this.startActivity(myIntent);
        }
    }
}
