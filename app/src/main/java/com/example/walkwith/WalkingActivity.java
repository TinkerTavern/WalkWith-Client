package com.example.walkwith;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WalkingActivity extends AppCompatActivity implements OnMapReadyCallback {

    GoogleMap gMap;
    SearchView searchView;
    Button alarmButton, startWalk, finishWalk;
    SupportMapFragment mapFragment;
    boolean active,onRoute;
    LatLng currentLocation, destination;
    String email,mode;
    Polyline route;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walking);
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapView2);
        mapFragment.getMapAsync(this);
        active = false;
        onRoute = false;

        Button alarmButton = findViewById(R.id.alarm_button);
        alarmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAlarm();
            }
        });

        startWalk = findViewById(R.id.start_walk);
        startWalk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                active = true;
                onRoute = true;
                Log.d("test", "onClick: ");
                //todo set visibility of different panels
                ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
                exec.scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        if (active) {
                            getUserLocation();
                            //todo check if on route
                            sendUpdateWalkPOST(email,Double.toString(currentLocation.latitude),Double.toString(currentLocation.longitude), Boolean.toString(onRoute));
                        }

                    }
                }, 0, 5, TimeUnit.SECONDS);
            }
        });

        finishWalk = findViewById(R.id.alarm_button);
        finishWalk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                active = false;
                //todo switch panels
                route.setVisible(false);
            }
        });

        searchView = findViewById(R.id.searchView);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String location = searchView.getQuery().toString();
                List<Address> list = null;
                Log.d("test1","before");
                Geocoder geocoder = new Geocoder(WalkingActivity.this);
                Log.d("test2","after");

                try {
                    list = geocoder.getFromLocationName(location, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (list!=null&&!list.isEmpty()) {
                    Address address = list.get(0);
                    destination = new LatLng(address.getLatitude(),address.getLongitude());
                    Marker marker = gMap.addMarker(new MarkerOptions().position(destination).title(location));
                    gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destination,10));
                    sendDetermineRoutePOST(email,mode,Double.toString(currentLocation.latitude),Double.toString(currentLocation.longitude),Double.toString(destination.latitude),Double.toString(destination.longitude));
                }
                else {
                    Log.d("test","failed attempt");
                }

                return true;
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

    public void sendDetermineRoutePOST(String email, String mode, String aLon,String aLat,String bLon,String bLat) {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = getResources().getString(R.string.server_ip) + "account";

        try {
            JSONObject jsonBody = new JSONObject();

            jsonBody.put("email", email);
            jsonBody.put("mode", mode);
            jsonBody.put("aLong", aLon);
            jsonBody.put("aLat", aLat);
            jsonBody.put("bLong", bLon);
            jsonBody.put("bLat", bLat);
            // Put your headers here

            JsonObjectRequest jsonObject = new JsonObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    openMenu();
                    showRoute();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // put the error here
                }
            });
            // Add the request to the RequestQueue.
            queue.add(jsonObject);

        } catch (
                JSONException e) {
            e.printStackTrace();
        }
    }

    private void showRoute()
    {
        //todo
    }

    private void openMenu()
    {

    }

    public void sendUpdateWalkPOST(String email, String lon, String lat, String onRoute) {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = getResources().getString(R.string.server_ip) + "account";

        try {
            JSONObject jsonBody = new JSONObject();

            jsonBody.put("email", email);
            jsonBody.put("long", lon);
            jsonBody.put("lat", lat);
            jsonBody.put("onRoute", onRoute);
            // Put your headers here

            JsonObjectRequest jsonObject = new JsonObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    //TODO update ETA and show new route
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // put the error here
                }
            });
            // Add the request to the RequestQueue.
            queue.add(jsonObject);

        } catch (
                JSONException e) {
            e.printStackTrace();
        }
    }

    private void getUserLocation() {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                double lat = location.getLatitude(), lon = location.getLongitude();
                currentLocation = new LatLng(lat,lon);
            } else {
                // Gone wrong
                Toast.makeText(this, "Error in getting location",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
