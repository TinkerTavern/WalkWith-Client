package com.example.walkwith;

import android.content.Context;
import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

public class AlarmActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendPost("hi", getApplicationContext());
            }
        });
    }
    protected void sendPost(String x, Context y) {
        RequestQueue queue = Volley.newRequestQueue(y);
        String url = getResources().getString(R.string.server_ip) + "/alarm";
        JSONObject jsonBody = new JSONObject();

        try {
            jsonBody.put("mode", "login");
            jsonBody.put("email", "email");
            jsonBody.put("password", "pass");

            JsonObjectRequest jsonObject = new JsonObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        response.get("result");
                        // success
                    } catch (JSONException e) {
                        // Error
                        e.getMessage();
                    }
                }
            },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.getMessage();
                        }
                    });
            queue.add(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
