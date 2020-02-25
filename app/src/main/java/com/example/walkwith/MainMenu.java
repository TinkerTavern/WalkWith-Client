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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;


import java.util.Objects;

public class MainMenu extends FragmentActivity implements View.OnClickListener, GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        OnMapReadyCallback{
    private GoogleMap mMap;
    private int MY_LOCATION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        try {
            Objects.requireNonNull(mapFragment).getMapAsync(this);
        } catch (NullPointerException e) {
            alertDialog("Map loading error", e.getMessage());
        }
        // Added simple toast
        Context context = getApplicationContext();
        CharSequence text = "Welcome to the app!";
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
        // New network test to see if server works
        networkTest();

        Button viewSettings = findViewById(R.id.button); //Settings button
        Button viewTrustedContacts = findViewById(R.id.button2); //Trusted Contacts button
        Button viewActiveWalkers = findViewById(R.id.button3); //Active Walkers button
        Button startNewWalk = findViewById(R.id.button4); //New Walk button

        viewSettings.setOnClickListener(this);
        viewTrustedContacts.setOnClickListener(this);
        viewActiveWalkers.setOnClickListener(this);
        startNewWalk.setOnClickListener(this);
    }

        @Override
        public void onClick(View v){
            switch (v.getId()) {
                case R.id.button:
                    openSettings();
                case R.id.button2:
                    viewTrustedContacts();
                case R.id.button3:
                    viewActiveWalkers();
                case R.id.button4:
                    openWalking();
            }
        }

    protected void openSettings(){
        Intent openSettings = new Intent(this, SettingsActivity.class);
        startActivity(openSettings);
    }

    protected void viewTrustedContacts() {
        Intent openContacts = new Intent (this, TrustedContacts.class);
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

    private void networkTest() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String ip = "138.38.149.100"; // Replace this with your own
        String port = "5000"; // Usually this
        String url = "http://" + ip + ":" + port + "/ping";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        alertDialog("Network Connection Success!", response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                alertDialog("Network connection failed", "Reason: " + error);
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
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
}
