package com.example.walkwith;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import com.example.walkwith.utils.Utilities;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class MainMenu extends FragmentActivity implements View.OnClickListener, GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener, TaskLoadedCallback,
        OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private GoogleMap mMap;
    private int MY_LOCATION_REQUEST_CODE = 1;

    //Random Friends
    private MarkerOptions place1, place2;

    Polyline route;
    Thread trustThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        try {
            Objects.requireNonNull(mapFragment).getMapAsync(this);
        } catch (NullPointerException e) {
            alertDialog("Map loading error", e.getMessage());
        }
        Button viewSettings = findViewById(R.id.settings); //Settings button
        Button viewTrustedContacts = findViewById(R.id.trustedContacts); //Trusted Contacts button
        Button viewActiveWalkers = findViewById(R.id.activeWalkers); //Active Walkers button
        Button startNewWalk = findViewById(R.id.startWalk); //New Walk button

        viewSettings.setOnClickListener(this);
        viewTrustedContacts.setOnClickListener(this);
        viewActiveWalkers.setOnClickListener(this);
        startNewWalk.setOnClickListener(this);

        trustThread = new  Thread() {
            public void run() {
                // do stuff
                while (true) {
                    sendPOST(AccountInfo.getEmail());
                    try {
                        Thread.sleep(Integer.parseInt(getResources().getString(R.string.idleTimer)));
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        };
        trustThread.start();

        place1 = new MarkerOptions().position(new LatLng(27.658143, 85.3199503)).title("Location 1");
        place2 = new MarkerOptions().position(new LatLng(27.667491, 85.3208583)).title("Location 2");
    }

    @Override
    public void onClick(View v){
        switch (v.getId()) {
            case R.id.settings:
                openSettings();
                break;
            case R.id.trustedContacts:
                viewTrustedContacts();
                break;
            case R.id.activeWalkers:
                viewActiveWalkers();
                break;
            case R.id.startWalk:
                openWalking();
                break;
        }
    }

    protected void openSettings(){
        Intent openSettings = new Intent(this, SettingsActivity.class);
        startActivity(openSettings);
    }

    protected void viewTrustedContacts() {
        Intent openContacts = new Intent (this, TrustedContactList.class);
        startActivity(openContacts);
    }

    protected void viewActiveWalkers() {
        Intent viewWalkers = new Intent (this, FocusView.class);
        startActivity(viewWalkers);
    }

    protected void openWalking(){
        Intent openWalking = new Intent(this, WalkingActivity.class);
        startActivity(openWalking);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Get users last location to show on map
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
            checkIfLocationOn();
        else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_LOCATION_REQUEST_CODE);
        }

        //Adding friends to map

        //TODO UNCOMMENT THIS TO USE ACTUAL FRIEND'S AND USER DETAILS
        Toast.makeText(this, "Hello, " + AccountInfo.getEmail(),
                Toast.LENGTH_SHORT).show();

        //This is for testing use line up top
        //displayTrustedContactLoc(new String[]{"a", "b", "c"}, new int[]{52, 32, 76}, new double[]{2.1, 3.2, 4.3});

        mMap.setOnMarkerClickListener(this);

        String url = getUrl(place1.getPosition(), place2.getPosition(), "walking");

        //UNCOMMENT THIS TO SHOW ROUTE BETWEEN 2 POINTS
        //new FetchURL(MainMenu.this).execute(url,"walking");

        //Simple line between 2 points
        Polyline polyline1 = mMap.addPolyline(new PolylineOptions()
                .clickable(true)
                .add(
                        new LatLng(51, 2.3),
                        new LatLng(51, 2.6)));

        mMap.addMarker(place1);
        mMap.addMarker(place2);

        // Set listeners for click events.
        /*googleMap.setOnPolylineClickListener(this);
        googleMap.setOnPolygonClickListener(this);*/

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_LOCATION_REQUEST_CODE) {
            if (permissions.length == 1 &&
                    permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION) &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED)
                checkIfLocationOn();
            else
                Toast.makeText(this, "Location services not allowed, functionality reduced", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onMyLocationButtonClick() {
//        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    public void enableLocationThings() {
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        //TODO: Make it zoom to current location
    }

    public static Boolean isLocationEnabled(Context context)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // This is new method provided in API 28
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (lm != null)
                return lm.isLocationEnabled();
            else
                return false;
        }
        else {
            // This is Deprecated in API 28
            int mode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE,
                    Settings.Secure.LOCATION_MODE_OFF);
            return  (mode != Settings.Secure.LOCATION_MODE_OFF);
        }
    }

    private void checkIfLocationOn() {
        if (isLocationEnabled(getApplicationContext()))
            enableLocationThings();
        else {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int id) {
                            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                            if (isLocationEnabled(getApplicationContext()))
                                enableLocationThings();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int id) {
                            dialog.cancel();
                        }
                    });
            final AlertDialog alert = builder.create();
            alert.show();
        }
    }

    private void alertDialog(final String title, final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (!isFinishing()) {
                    new AlertDialog.Builder(com.example.walkwith.MainMenu.this)
                            .setTitle(title)
                            .setMessage(message)
                            .setCancelable(false)
                            .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Whatever...
                                }
                            }).show();
                }
            }
        });
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        AccountInfo.setFriendFocusedOn(marker.getTitle());
        Intent focusView = new Intent (this, FocusView.class);
        startActivity(focusView);
        return false;
    }

    private String getUrl(LatLng origin, LatLng dest, String directionMode) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Mode
        String mode = "mode=" + directionMode;
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + "AIzaSyBduaZIXEGGMPnEXcYQERJS5pFOvCG0i20";
        //TODO Why is the key written here in plaintext?
        return url;
    }

    @Override
    public void onTaskDone(Object... values) {
        if (route != null)
            route.remove();
        route = mMap.addPolyline((PolylineOptions) values[0]);
    }

    private void sendPOST(String email) {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = getResources().getString(R.string.server_ip) + "updateView";
        try {
            JSONObject jsonBody = new JSONObject();

            jsonBody.put("email", email);

            JsonObjectRequest jsonObject = new JsonObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {

                        Double[] Longs = Utilities.jsonArrayToList((JSONArray) response.get("longs")).toArray(new Double[0]);
                        Double[] Lats = Utilities.jsonArrayToList((JSONArray) response.get("lats")).toArray(new Double[0]);
                        String[] emails = Utilities.jsonArrayToList((JSONArray) response.get("emails")).toArray(new String[0]);
                        displayTrustedContactLoc(emails, Lats, Longs);
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

    private void displayTrustedContactLoc(String[] emails, Double[] lats, Double[] longs){
        Marker mFriend;

        for(int i = 0; i < emails.length; i++){
            mFriend = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(lats[i], longs[i]))
                    .title(emails[i])
            );
            mFriend.setTag(0);
        }
    }
}
