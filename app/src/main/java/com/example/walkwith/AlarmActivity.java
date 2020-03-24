package com.example.walkwith;

import android.content.SharedPreferences;
import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.walkwith.utils.Utilities;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class AlarmActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);
        Toolbar toolbar = findViewById(R.id.toolbar);
        SharedPreferences preferences = getSharedPreferences("checkbox", MODE_PRIVATE);
        String theme = preferences.getString("theme","");
        if (!theme.equals(""))
            AppCompatDelegate.setDefaultNightMode(Integer.parseInt(theme));
        setSupportActionBar(toolbar);
        final Button alertContactButton = findViewById(R.id.alertContacts);
        alertContactButton.setOnClickListener(view -> {
            alertPOST(AccountInfo.getEmail(),
                    AccountInfo.getEmail() + " is in danger!");
        });
    }

    private void alertPOST(String email, String message) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = getResources().getString(R.string.server_ip) + "alarm";
        try {
            JSONObject jsonBody = new JSONObject();

            jsonBody.put("email", email);
            jsonBody.put("message", message);

            JsonObjectRequest jsonObject = new JsonObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        String result = (String) response.get("result");
                        if (result.equals("True"))
                            result = "Successfully alarmed contacts";
                        else
                            result = "Error in alerting contacts";
                        Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();

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
}
