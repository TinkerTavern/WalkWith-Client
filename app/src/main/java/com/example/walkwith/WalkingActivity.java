package com.example.walkwith;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.example.walkwith.MainMenu.isLocationEnabled;

public class WalkingActivity extends AppCompatActivity implements GoogleMap.OnMyLocationClickListener, GoogleMap.OnMyLocationButtonClickListener, OnMapReadyCallback {

    private GoogleMap mMap;
    private SearchView searchView;
    private Button alarmButton, startWalk, finishWalk, back;
    private SupportMapFragment mapFragment;
    private Switch safeWalk, lightWalk, torch;
    private boolean active, onRoute, gotLocation,
            safeRoute = false,  lightRoute = false, cameraWorking = false, searched = false;
    private LatLng currentLocation, destination;
    private String email, cameraId;
    private Polyline line;
    private PolylineOptions route;
    private LocationManager mLocationManager;
    private List<LatLng> points;
    private CameraManager camManager;
    private Marker endPoint;
    private Double latitude, longitude, LATLONG_DEFAULT = 0d;
    private Float zoom, ZOOM_DEFAULT = 15f;
    private Double endLat, endLong;
    private TextView eta, distanceLeft;
    /*
    0 - Normal Route
    1 - Safe route
    2 - Light route
    3 - Both
     */

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walking);
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapView2);

        eta = findViewById(R.id.eta);
        distanceLeft = findViewById(R.id.distanceLeft);
        eta.setText(getString(R.string.eta, "-"));
        distanceLeft.setText(getString(R.string.distanceLeft, "-"));

        zoom = getIntent().getFloatExtra("cameraZoom", ZOOM_DEFAULT);
        latitude = getIntent().getDoubleExtra("cameraLat", LATLONG_DEFAULT);
        longitude = getIntent().getDoubleExtra("cameraLong", LATLONG_DEFAULT);
        try {
            Objects.requireNonNull(mapFragment).getMapAsync(this);
        } catch (NullPointerException e) {
            Toast.makeText(this, "Maps Error.", Toast.LENGTH_SHORT).show();
        }
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        email = AccountInfo.getEmail();
        if (email == null) {
            SharedPreferences preferences = getSharedPreferences("checkbox", MODE_PRIVATE);
            String newEmail = preferences.getString("email", "");
            if (!newEmail.equals(""))
                email = newEmail;
            else {
                Toast.makeText(this, "lost email?", Toast.LENGTH_SHORT).show();
            }
        }
        active = false;
        onRoute = false;
        route = new PolylineOptions().
                geodesic(true).
                color(Color.BLUE).
                width(10);

        safeWalk = findViewById(R.id.safeWalk);
        lightWalk = findViewById(R.id.lightWalk);
        torch = findViewById(R.id.torch);
        setupCamera();

        alarmButton = findViewById(R.id.alarm_button);
        alarmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAlarm();
            }
        });

        back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                destination = null;
                searched = false;
                if (line != null)
                    line.remove();
                back.setVisibility(View.GONE);
                startWalk.setVisibility(View.GONE);
                distanceLeft.setVisibility(View.GONE);
                eta.setVisibility(View.GONE);
                lightWalk.setVisibility(View.VISIBLE);
                safeWalk.setVisibility(View.VISIBLE);
                torch.setVisibility(View.GONE);
            }
        });

        startWalk = findViewById(R.id.start_walk);
        startWalk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                active = true;
                onRoute = true;
                Log.d("test", "onClick: ");
                back.setVisibility(View.GONE);
                startWalk.setVisibility(View.GONE);
                finishWalk.setVisibility(View.VISIBLE);
                searchView.setVisibility(View.GONE);
                lightWalk.setVisibility(View.GONE);
                distanceLeft.setVisibility(View.VISIBLE);
                eta.setVisibility(View.VISIBLE);
                safeWalk.setVisibility(View.GONE);
                torch.setVisibility(View.VISIBLE);
                new Thread() {
                    @Override
                    public void run() {
                        while (true) {
                            if (active) {
                                getLastBestLocation();
                                onRoute = checkIfOnRoute(100);
                                sendUpdateWalkPOST(email, Double.toString(currentLocation.latitude),
                                        Double.toString(currentLocation.longitude),
                                        Boolean.toString(onRoute), "1");
                                onRoute = true;
                            }
                            else
                                break;
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException e) {
                                break;
                            }
                        }
                    }
                }.start();
            }
        });

        torch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (cameraWorking)
                try {
                    if (buttonView.isChecked())
                        camManager.setTorchMode(cameraId, true);
                    else
                        camManager.setTorchMode(cameraId, false);
                }
                catch (CameraAccessException e) {
                    Toast.makeText(getApplicationContext(), "Can't access camera for flash",
                            Toast.LENGTH_SHORT).show();
                }
        });

        safeWalk.setOnCheckedChangeListener((buttonView, isChecked) -> {
            safeRoute = !safeRoute;
            if (buttonView.isChecked())
                Toast.makeText(getApplicationContext(), "Will specifically avoid higher " +
                                "crime rate areas", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(getApplicationContext(), "Will not consider crime rates",
                        Toast.LENGTH_SHORT).show();
            if (searched)
                searchView.setQuery(searchView.getQuery(), true);
        });

        lightWalk.setOnCheckedChangeListener((buttonView, isChecked) -> {
            lightRoute = !lightRoute;
            if (buttonView.isChecked())
                Toast.makeText(getApplicationContext(), "Will try to take you on the" +
                                " lightest route", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(getApplicationContext(), "Will not consider lighting",
                        Toast.LENGTH_SHORT).show();
            if (searched)
                searchView.setQuery(searchView.getQuery(), true);
        });

        finishWalk = findViewById(R.id.finish_walk);
        finishWalk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopWalk();
            }
        });

        searchView = findViewById(R.id.searchView);
        searchView.setIconified(false);
        //The above line will expand it to fit the area as well as throw up the keyboard

        //To remove the keyboard, but make sure you keep the expanded version:
        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                //TODO make this a thread
                getLastBestLocation();
                String location = searchView.getQuery().toString();
                List<Address> list = null;
                Geocoder geocoder = new Geocoder(WalkingActivity.this);

                try {
                    list = geocoder.getFromLocationName(location, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (list != null && !list.isEmpty()) {
                    back.setVisibility(View.VISIBLE);
                    startWalk.setVisibility(View.VISIBLE);
                    torch.setVisibility(View.GONE);

                    Address address = list.get(0);
                    Log.d("test", address.getAddressLine(0));
                    destination = new LatLng(address.getLatitude(), address.getLongitude());
                    endPoint = mMap.addMarker(new MarkerOptions().position(destination).title(location));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destination, 15f));
                    sendDetermineRoutePOST(email, Double.toString(currentLocation.latitude), Double.toString(currentLocation.longitude), Double.toString(destination.latitude), Double.toString(destination.longitude));
                    searched = true;
                } else {
                    Log.d("test", "failed attempt");
                }

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        finishWalk.setVisibility(View.GONE);
        back.setVisibility(View.GONE);
        startWalk.setVisibility(View.GONE);
        distanceLeft.setVisibility(View.GONE);
        eta.setVisibility(View.GONE);
        lightWalk.setVisibility(View.VISIBLE);
        safeWalk.setVisibility(View.VISIBLE);
        torch.setVisibility(View.GONE);
    }

    private void setupCamera() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M)
            Toast.makeText(this, "Torch can't be accessed before android " +
                    "Marshmallow", Toast.LENGTH_SHORT);
        else {
            try {
                camManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
                cameraId = camManager.getCameraIdList()[0]; // Usually front camera is at 0 position.
                cameraWorking = true;
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Can't access camera for flash",
                        Toast.LENGTH_SHORT).show();
            }
        }
        torch.setClickable(cameraWorking);
    }

    protected void openAlarm() {
        Intent viewAlarm = new Intent(this, AlarmActivity.class);
        startActivity(viewAlarm);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        getLastBestLocation();
        View locationButton = ((View) findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
        View view = findViewById(R.id.searchView);
        int[] loc = {0,0};
        locationButton.getLocationOnScreen(loc);
        int absoluteBottom = loc[1] + view.getHeight();
        rlp.setMargins(0, absoluteBottom, 5, 0);

        if (!zoom.equals(ZOOM_DEFAULT) && !latitude.equals(LATLONG_DEFAULT) &&
                !longitude.equals(LATLONG_DEFAULT)) {
            LatLng latLng = new LatLng(latitude, longitude);
            CameraUpdate location = CameraUpdateFactory.newLatLngZoom(
                    latLng, zoom);
            mMap.moveCamera(location);
        }
        else {
            Toast.makeText(getApplicationContext(), "Map consistency error",
                    Toast.LENGTH_SHORT).show();
            if (currentLocation != null) {
                CameraUpdate location = CameraUpdateFactory.newLatLngZoom(
                        currentLocation, 15f);
                mMap.moveCamera(location);
            }
        }
    }

    private String determineMode() {
        int mode = 0;
        if (safeRoute)
            mode++;
        if (lightRoute)
            mode += 2;
        return String.valueOf(mode);
    }

    public void sendDetermineRoutePOST(String email, String aLat, String aLon, String bLat, String bLong) {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = getResources().getString(R.string.server_ip) + "determineRoute";

        try {
            JSONObject jsonBody = new JSONObject();

            jsonBody.put("email", email);
            jsonBody.put("mode", determineMode());
            jsonBody.put("aLong", aLon);
            jsonBody.put("aLat", aLat);
            jsonBody.put("bLong", bLong);
            jsonBody.put("bLat", bLat);
            // Put your headers here
            Log.d("test", aLat+", "+aLon+", "+bLat+", "+bLong);

            JsonObjectRequest jsonObject = new JsonObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    showRoute(response, bLat, bLong);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // put the error here
                    Toast.makeText(getApplicationContext(), "Routing issues", Toast.LENGTH_SHORT).show();
                }
            });
            // Add the request to the RequestQueue.
            queue.add(jsonObject);

        } catch (
                JSONException e) {
            Toast.makeText(getApplicationContext(), "Routing issues", Toast.LENGTH_SHORT).show();
        }
    }

    private void showRoute(JSONObject obj, String bLat, String bLong) {
        try {
            if (line != null)
                line.remove();
            String result = (String) obj.get("result");
            Log.d("test", result);

            if (result.equals("True-But No Safe Route"))
                Toast.makeText(this, "No safer route found",
                        Toast.LENGTH_SHORT).show();

            else if (!result.equals("True")) {
                Toast.makeText(this, "Couldn't determine route. Try again later.",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            endLat = Double.parseDouble(bLat);
            endLong = Double.parseDouble(bLong);
            JSONObject overview_polylineJson = obj.getJSONObject("overview_polyline");
            points = decodePolyLine(overview_polylineJson.getString("points"));
            route = new PolylineOptions().
                    geodesic(true).
                    color(Color.BLUE).
                    width(10);
            for (int i = 0; i < points.size(); i++)
                route.add(points.get(i));
            line = mMap.addPolyline(route);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private boolean checkIfOnRoute(double maxDistanceAway) {
        for(int i = 0;i<points.size();i++){
            double dist = distance(points.get(i).latitude,points.get(i).longitude,currentLocation.latitude,currentLocation.longitude);
            if(dist<maxDistanceAway)
                return true;
        }
        return false;
    }

    private List<LatLng> decodePolyLine(String routePoints) {

        //Log.d("DECOOOOOOOODE", routePoints);
        int len = routePoints.length();
        int index = 0;
        List<LatLng> decoded = new ArrayList<LatLng>();
        int lat = 0;
        int lng = 0;

        while (index < len) {
            int b;
            int shift = 0;
            int result = 0;
            do {
                b = routePoints.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = routePoints.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng point = new LatLng(
                    lat / 100000d, lng / 100000d
            );
            //Log.d("POOOOOINT", point.latitude+" "+point.longitude);
            decoded.add(point);
        }

        return decoded;
    }

    public double distance (double lat_a, double lng_a, double lat_b, double lng_b )
    {
        double earthRadius = 3958.75;
        double latDiff = Math.toRadians(lat_b-lat_a);
        double lngDiff = Math.toRadians(lng_b-lng_a);
        double a = Math.sin(latDiff /2) * Math.sin(latDiff /2) +
                Math.cos(Math.toRadians(lat_a)) * Math.cos(Math.toRadians(lat_b)) *
                        Math.sin(lngDiff /2) * Math.sin(lngDiff /2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double distance = earthRadius * c;

        int meterConversion = 1609;

        return new Double(distance * meterConversion).doubleValue();
    }

    public void sendUpdateWalkPOST(String email, String lat, String lon, String onRoute, String isActive) {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = getResources().getString(R.string.server_ip) + "walkUpdate";

        try {
            JSONObject jsonBody = new JSONObject();

            jsonBody.put("email", email);
            jsonBody.put("aLong", lon);
            jsonBody.put("aLat", lat);
            jsonBody.put("mode", determineMode());
            jsonBody.put("bLong", endLong);
            jsonBody.put("bLat", endLat);
            jsonBody.put("onRoute", onRoute);
            jsonBody.put("isActive", isActive);
            // Put your headers here

            JsonObjectRequest jsonObject = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                    new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        String result = (String) response.get("result");
                        if (result.equals("True")) {
                            int ETA = (Integer) response.get("ETA");
                            int mins = ETA/60;
                            int secs = ETA%60;
                            String ETAString = mins + ":" + secs;
                            eta.setText(getString(R.string.eta, ETAString));
                            Integer distance = (Integer) response.get("distance");
                            String dist = String.valueOf(distance) + "m";
                            distanceLeft.setText(dist);
                            if (ETA < 30 || distance < 10)
                                askToStop();
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Maps Error.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(getApplicationContext(), "Routing error.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getApplicationContext(), "Routing error.",
                            Toast.LENGTH_SHORT).show();
                }
            });
            // Add the request to the RequestQueue.
            queue.add(jsonObject);

        } catch (
                JSONException e) {
            Toast.makeText(getApplicationContext(), "Routing error.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void stopWalk() {
        searched = false;
        active = false;
        finishWalk.setVisibility(View.GONE);
        searchView.setVisibility(View.VISIBLE);
        lightWalk.setVisibility(View.VISIBLE);
        safeWalk.setVisibility(View.VISIBLE);
        distanceLeft.setVisibility(View.GONE);
        eta.setVisibility(View.GONE);
        torch.setVisibility(View.GONE);
        if (endPoint != null)
            endPoint.remove();
        if (line != null)
            line.remove();
        sendUpdateWalkPOST(email, Double.toString(currentLocation.latitude),
                Double.toString(currentLocation.longitude),
                Boolean.toString(onRoute), "0");
    }

    private void askToStop() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Have you arrived");
        builder.setMessage("We have detected you are near your destination, do you wish to stop " +
                "tracking?");
        // Set up the buttons
        builder.setPositiveButton("Finished Walk", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                stopWalk();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                Toast.makeText(getApplicationContext(), "Walk will continue",
                        Toast.LENGTH_SHORT).show();
            }
        });
        builder.show();
    }

    private void getLastBestLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.
                ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.
                        ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;
        Location locationGPS = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location locationNet = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        long GPSLocationTime = 0;
        if (null != locationGPS)
            GPSLocationTime = locationGPS.getTime();

        long NetLocationTime = 0;

        if (null != locationNet)
            NetLocationTime = locationNet.getTime();

        if ( 0 < GPSLocationTime - NetLocationTime )
            currentLocation = new LatLng(locationGPS.getLatitude(), locationGPS.getLongitude());
        else {
            try {
                currentLocation = new LatLng(locationNet.getLatitude(), locationNet.getLongitude());
            }
            catch (NullPointerException e) {
                Log.e("Error", "Oof");
//                Toast.makeText(this, "Location error", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {

    }
}
