package com.example.walkwith;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class WalkingActivity extends AppCompatActivity implements OnMapReadyCallback {

    GoogleMap gMap;
    SearchView searchView;
    SupportMapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walking);
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapView2);
        mapFragment.getMapAsync(this);

        Button alarmButton = findViewById(R.id.alarm_button);
        alarmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAlarm();
            }
        });

        searchView = findViewById(R.id.searchView);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String location = searchView.getQuery().toString();
                List<Address> list = null;

                Geocoder geocoder = new Geocoder(WalkingActivity.this);

                try {
                    list = geocoder.getFromLocationName(location, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Address destination = list.get(0);
                //todo something with the location

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }
    protected void openAlarm(){
        Intent viewAlarm = new Intent(this, AlarmActivity.class);
        startActivity(viewAlarm);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
    }

    public void sendUpdateWalkPOST(String email, String mode, String aLon,String aLat,String bLon,String bLat, final Context parentContext) {
        RequestQueue queue = Volley.newRequestQueue(parentContext);
        String ip = "138.38.223.205"; // Replace this with your own
        String port = "5000"; // Usually this
        String url = "http://" + ip + ":" + port + "/account";

        try {
            JSONObject jsonBody = new JSONObject();

            jsonBody.put("email", email);
            jsonBody.put("mode", mode);
            jsonBody.put("aLong", aLon);
            jsonBody.put("aLat", aLat);
            jsonBody.put("bLong", bLon);
            jsonBody.put("bLat", aLat);
            // Put your headers here

            JsonObjectRequest jsonObject = new JsonObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    //TODO start walk
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // put the error here
                    // TODO
                }
            });
            // Add the request to the RequestQueue.
            queue.add(jsonObject);

        } catch (
                JSONException e) {
            e.printStackTrace();
        }
    }
}
