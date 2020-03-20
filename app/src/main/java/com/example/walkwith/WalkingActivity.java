package com.example.walkwith;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Switch;
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

public class WalkingActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap gMap;
    private SearchView searchView;
    private Button alarmButton, startWalk, finishWalk, back;
    private SupportMapFragment mapFragment;
    private Switch safeWalk, lightWalk, torch;
    private boolean active, onRoute, gotLocation,
            safeRoute = false,  lightRoute = false, cameraWorking = false;
    private LatLng currentLocation, destination;
    private String email, cameraId;
    private Polyline line;
    private PolylineOptions route;
    private Location currentBestLocation = null;
    private LocationManager mLocationManager;
    private List<LatLng> points;
    private CameraManager camManager;
    private Double latitude, longitude, LATLONG_DEFAULT = 0d;
    private Float zoom, ZOOM_DEFAULT = 15f;
    /*
    0 - Normal Route
    1 - Safe route
    2 - Light route
    3 - Both
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walking);
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapView2);

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
                if (line != null)
                    line.remove();
                back.setVisibility(View.GONE);
                startWalk.setVisibility(View.GONE);
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
                safeWalk.setVisibility(View.GONE);
                torch.setVisibility(View.VISIBLE);
                ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
                exec.scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        if (active) {
                            getLastBestLocation();
                            onRoute = checkIfOnRoute(100);
                            sendUpdateWalkPOST(email, Double.toString(currentLocation.latitude),
                                    Double.toString(currentLocation.longitude),
                                    Boolean.toString(onRoute), "1");
                            onRoute = true;
                        }

                    }
                }, 0, 5, TimeUnit.SECONDS);
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
            if (buttonView.isChecked()) {
                Toast.makeText(getApplicationContext(), "Will specifically avoid higher " +
                                "crime rate areas", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Will not consider crime rates",
                        Toast.LENGTH_SHORT).show();
            }
        });

        lightWalk.setOnCheckedChangeListener((buttonView, isChecked) -> {
            lightRoute = !lightRoute;
            if (buttonView.isChecked()) {
                Toast.makeText(getApplicationContext(), "Will try to take you on the" +
                                " lightest route", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Will not consider lighting",
                        Toast.LENGTH_SHORT).show();
            }
        });

        finishWalk = findViewById(R.id.finish_walk);
        finishWalk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                active = false;
                finishWalk.setVisibility(View.GONE);
                searchView.setVisibility(View.VISIBLE);
                lightWalk.setVisibility(View.VISIBLE);
                safeWalk.setVisibility(View.VISIBLE);
                torch.setVisibility(View.GONE);
                if (line != null)
                    line.remove();
                sendUpdateWalkPOST(email, Double.toString(currentLocation.latitude),
                        Double.toString(currentLocation.longitude),
                        Boolean.toString(onRoute), "0");
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
                    lightWalk.setVisibility(View.GONE);
                    safeWalk.setVisibility(View.GONE);
                    torch.setVisibility(View.GONE);

                    Address address = list.get(0);
                    Log.d("test", address.getAddressLine(0));
                    destination = new LatLng(address.getLatitude(), address.getLongitude());
                    Marker marker = gMap.addMarker(new MarkerOptions().position(destination).title(location));
                    gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destination, 10));
                    sendDetermineRoutePOST(email, Double.toString(currentLocation.latitude), Double.toString(currentLocation.longitude), Double.toString(destination.latitude), Double.toString(destination.longitude));
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
        lightWalk.setVisibility(View.VISIBLE);
        safeWalk.setVisibility(View.VISIBLE);
        torch.setVisibility(View.GONE);
    }

    private void setupCamera() {
        try {
            camManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            cameraId = camManager.getCameraIdList()[0]; // Usually front camera is at 0 position.
            cameraWorking = true;
        }
        catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Can't access camera for flash",
                    Toast.LENGTH_SHORT).show();
        }
        torch.setClickable(cameraWorking);
    }

    protected void openAlarm() {
        Intent viewAlarm = new Intent(this, AlarmActivity.class);
        startActivity(viewAlarm);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;

        if (!zoom.equals(ZOOM_DEFAULT) && !latitude.equals(LATLONG_DEFAULT) &&
                !longitude.equals(LATLONG_DEFAULT)) {
            LatLng latLng = new LatLng(latitude, longitude);
            CameraUpdate location = CameraUpdateFactory.newLatLngZoom(
                    latLng, zoom);
            gMap.animateCamera(location);
        }
        else
            Toast.makeText(getApplicationContext(), "Map consistency error",
                    Toast.LENGTH_SHORT).show();
        //hardcoded test for the show route method
        /*getUserLocation();
        line = gMap.addPolyline(route);
        MarkerOptions place1 = new MarkerOptions().position(new LatLng(27.658143, 85.3199503)).title("Location 1");
        MarkerOptions place2 = new MarkerOptions().position(new LatLng(27.667491, 85.3208583)).title("Location 2");
        gMap.addMarker(place1);
        gMap.addMarker(place2);
        String everything = "{\"bounds\": {\"northeast\": {\"lat\": 27.6674286, \"lng\": 85.3213586}, \"southwest\": {\"lat\": 27.6579578, \"lng\": 85.3181149}}, \"copyrights\": \"Map data \\u00a92020\", \"legs\": [{\"distance\": {\"text\": \"1.3 km\", \"value\": 1343}, \"duration\": {\"text\": \"16 mins\", \"value\":\n" +
                "979}, \"end_address\": \"Hospital Rd, Lalitpur, Nepal\", \"end_location\": {\"lat\": 27.6673604, \"lng\": 85.3208903}, \"start_address\": \"Unnamed Road, Lalitpur 44700, Nepal\", \"start_location\": {\"lat\": 27.6581039, \"lng\": 85.31993609999999}, \"steps\": [{\"distance\":\n" +
                "{\"text\": \"41 m\", \"value\": 41}, \"duration\": {\"text\": \"1 min\", \"value\": 28}, \"end_location\": {\"lat\": 27.6579578, \"lng\": 85.3203163}, \"html_instructions\": \"Head <b>east</b>\", \"polyline\": {\"points\": \"c~xgDs`wgO@EFYDQJY\"}, \"start_location\": {\"lat\": 27.6581039,\n" +
                "\"lng\": 85.31993609999999}, \"travel_mode\": \"WALKING\"}, {\"distance\": {\"text\": \"0.1 km\", \"value\": 127}, \"duration\": {\"text\": \"1 min\", \"value\": 81}, \"end_location\": {\"lat\": 27.6590027, \"lng\": 85.32078349999999}, \"html_instructions\": \"Turn <b>left</b>\", \"maneuver\":\n" +
                "\"turn-left\", \"polyline\": {\"points\": \"g}xgD_cwgO_@[k@Wq@Q]Is@K\"}, \"start_location\": {\"lat\": 27.6579578, \"lng\": 85.3203163}, \"travel_mode\": \"WALKING\"}, {\"distance\": {\"text\": \"0.4 km\", \"value\": 358}, \"duration\": {\"text\": \"4 mins\", \"value\": 260}, \"end_location\":\n" +
                "{\"lat\": 27.6611848, \"lng\": 85.3181149}, \"html_instructions\": \"Turn left\", \"maneuver\": \"turn-left\", \"polyline\": {\"points\": \"wcygD{ewgOmB`CcBtBKLiAdB_AlAkAzA\"}, \"start_location\": {\"lat\": 27.6590027, \"lng\": 85.32078349999999}, \"travel_mode\": \"WALKING\"},\n" +
                "{\"distance\": {\"text\": \"0.8 km\", \"value\": 768}, \"duration\": {\"text\": \"10 mins\", \"value\": 572}, \"end_location\": {\"lat\": 27.6673496, \"lng\": 85.3213586}, \"html_instructions\": \"Turn <b>right</b> onto <b>Mahalaxmisthan Rd</b>\", \"maneuver\": \"turn-right\", \"polyline\":\n" +
                "{\"points\": \"kqygDeuvgOUIUGIIKIMKAAu@s@_@]AAuB_BoBkAKUEEKIIEa@MME]MiASo@Qe@Ii@M_Ca@QEsBi@q@QCA[MICo@WeAc@KE]O\"}, \"start_location\": {\"lat\": 27.6611848, \"lng\": 85.3181149}, \"travel_mode\": \"WALKING\"}, {\"distance\": {\"text\": \"49 m\", \"value\": 49}, \"duration\":\n" +
                "{\"text\": \"1 min\", \"value\": 38}, \"end_location\": {\"lat\": 27.6673604, \"lng\": 85.3208903}, \"html_instructions\": \"Turn Left\", \"maneuver\": \"turn-left\", \"polyline\": {\"points\": \"}wzgDoiwgOMp@AH@B?@?B@@@@@B?@@B@@@D?@\"}, \"start_location\": {\"lat\": 27.6673496, \"lng\": 85.3213586}, \"travel_mode\": \"WALKING\"}], \"traffic_speed_entry\": [], \"via_waypoint\":\n" +
                "[]}], \"overview_polyline\": {\"points\": \"c~xgDs`wgOH_@Pk@_@[k@WoA[s@KmB`CoBbCiAdB_AlAkAzAUI_@QYUyAuAuB_BoBkAQ[UOo@S}D}@iDo@eCo@u@Se@Q_DqAOz@@DDJDN\"}, \"summary\": \"Mahalaxmisthan Rd\", \"warnings\": [\"This route may be missing sidewalks or pedestrian paths.\"], \"waypoint_order\": [], \"result\": \"False\"}";
        try {

            JSONObject data = new JSONObject(everything);
            showRoute(data);
        } catch (JSONException e)
        {
            e.printStackTrace();
        }*/
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
                    Log.d("test", "response success");
                    showRoute(response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // put the error here
                    Log.d("error", error.toString());
                }
            });
            // Add the request to the RequestQueue.
            queue.add(jsonObject);

        } catch (
                JSONException e) {
            Log.d("exception", "wrong");
            e.printStackTrace();
        }
    }

    private void showRoute(JSONObject obj) {
        try {

            String result = (String) obj.get("result");
            Log.d("test", result);

            if (!result.equals("True")) {
                Toast.makeText(this, "Couldn't determine route. Try again later.",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            JSONObject overview_polylineJson = obj.getJSONObject("overview_polyline");
            points = decodePolyLine(overview_polylineJson.getString("points"));
            route = new PolylineOptions().
                    geodesic(true).
                    color(Color.BLUE).
                    width(10);
            for (int i = 0; i < points.size(); i++)
                route.add(points.get(i));
            line = gMap.addPolyline(route);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private boolean checkIfOnRoute(double maxDistanceAway) {
        for(int i = 0;i<points.size();i++){
            double dist = distance(points.get(i).latitude,points.get(i).longitude,currentLocation.latitude,currentLocation.longitude);
            if(dist<maxDistanceAway){
                return true;
            }
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

    public void sendUpdateWalkPOST(String email, String lon, String lat, String onRoute, String isActive) {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = getResources().getString(R.string.server_ip) + "walkUpdate";

        try {
            JSONObject jsonBody = new JSONObject();

            jsonBody.put("email", email);
            jsonBody.put("long", lon);
            jsonBody.put("lat", lat);
            jsonBody.put("onRoute", onRoute);
            jsonBody.put("isActive", isActive);
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

    private void getLastBestLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location locationGPS = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location locationNet = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        long GPSLocationTime = 0;
        if (null != locationGPS) { GPSLocationTime = locationGPS.getTime(); }

        long NetLocationTime = 0;

        if (null != locationNet) {
            NetLocationTime = locationNet.getTime();
        }

        if ( 0 < GPSLocationTime - NetLocationTime ) {
            currentLocation = new LatLng(locationGPS.getLatitude(), locationGPS.getLongitude());
        }
        else {
            currentLocation = new LatLng(locationNet.getLatitude(), locationNet.getLongitude());
        }
    }
}
