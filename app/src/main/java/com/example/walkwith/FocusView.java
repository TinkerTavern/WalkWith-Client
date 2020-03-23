package com.example.walkwith;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.walkwith.utils.Utilities;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.example.walkwith.MainMenu.isLocationEnabled;

public class FocusView extends FragmentActivity implements GoogleMap.OnMyLocationClickListener,
        GoogleMap.OnMyLocationButtonClickListener, OnMapReadyCallback,
        MyRecyclerViewAdapter.ItemClickListener{
    private GoogleMap mMap;
    private Polyline line;
    private boolean ready = false, firstTime = true;
    private PolylineOptions route;
    private LocationManager mLocationManager;
    private LatLng friendLocation, currentLocation;
    private Double latitude, longitude, LATLONG_DEFAULT = 0d;
    private Float zoom, ZOOM_DEFAULT = 15f;
    private boolean isActive = false;
    ArrayList<String> trustedContactNames = new ArrayList<>();
    private ArrayList<Marker> friendMarkers = new ArrayList<>();
    MyRecyclerViewAdapter adapter;
    RecyclerView recyclerView;
    private TextView focusFriend;
    private Button cancelFocus;

    private void changeActive(boolean status) {
        isActive = status;
        if (isActive) {
            recyclerView.setVisibility(View.VISIBLE);
            cancelFocus.setVisibility(View.GONE);
            focusFriend.setVisibility(View.GONE);
        }
        else {
            recyclerView.setVisibility(View.GONE);
            cancelFocus.setVisibility(View.VISIBLE);
            focusFriend.setVisibility(View.VISIBLE);
        }
    }

    private void getContactsTrusted() {
        trustedContactNames.addAll(AccountInfo.getContactsTrusted());
        for (String name: trustedContactNames) {
            Log.e("AAA", name);
        }
    }

    private void parseTheme(int theme) {
        ConstraintLayout someView = findViewById(R.id.focusConstraint);
        switch (theme) {
            case AppCompatDelegate.MODE_NIGHT_NO:
                someView.setBackgroundColor(ContextCompat.getColor(this, R.color.light));
                break;
            case AppCompatDelegate.MODE_NIGHT_YES:
                someView.setBackgroundColor(ContextCompat.getColor(this, R.color.dark));
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_focus_view);
        SharedPreferences preferences = getSharedPreferences("checkbox", MODE_PRIVATE);
        String theme = preferences.getString("theme","");
        if (!theme.equals(""))
            parseTheme(Integer.parseInt(theme));
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getContactsTrusted();
        recyclerView = findViewById(R.id.friendList);
        adapter = new MyRecyclerViewAdapter(this, trustedContactNames, 0);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
        cancelFocus = findViewById(R.id.stopFocus);
        focusFriend = findViewById(R.id.watching);
        cancelFocus.setOnClickListener(v -> {
            changeActive(true);
            AccountInfo.setFriendFocusedOn("");
        });

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        zoom = getIntent().getFloatExtra("cameraZoom", ZOOM_DEFAULT);
        String active = getIntent().getStringExtra("isActive");
        if (active != null && active.equals("True")) {
            changeActive(true);
        }
        latitude = getIntent().getDoubleExtra("cameraLat", LATLONG_DEFAULT);
        longitude = getIntent().getDoubleExtra("cameraLong", LATLONG_DEFAULT);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapView2);
        try {
            Objects.requireNonNull(mapFragment).getMapAsync(this);
        }
        catch (NullPointerException e) {
            alertDialog("Map loading error", e.getMessage());
        }

        Thread trustThread = new  Thread() {
            public void run() {
                // do stuff
                int sleepTime;
                while (true) {
                    if (isActive)
                        sleepTime = Integer.parseInt(getResources().getString(R.string.activeTimer));
                    else
                        sleepTime = Integer.parseInt(getResources().getString(R.string.focusTimer));
                    if (ready)
                        updateViewPOST(AccountInfo.getEmail());
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        };
        trustThread.start();
    }

    public void onMapReady(GoogleMap map){
        mMap = map;
        ready = true;
        getLastBestLocation();
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        if (AccountInfo.getFriendFocusedOn() != null)
            sendGetFriendRoutePOST(AccountInfo.getEmail(),AccountInfo.getFriendFocusedOn());
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

    private void updateViewPOST(String email) { // TODO Make this a general function
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = getResources().getString(R.string.server_ip) + "updateView";
        try {
            JSONObject jsonBody = new JSONObject();

            if (email == null) {
                SharedPreferences preferences = getSharedPreferences("checkbox", MODE_PRIVATE);
                String newEmail = preferences.getString("email", "");
                if (!newEmail.equals(""))
                    email = newEmail;
                else {
                    Toast.makeText(this, "lost email?", Toast.LENGTH_SHORT).show();
                }
            }
            jsonBody.put("email", email);

            JsonObjectRequest jsonObject = new JsonObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onResponse(JSONObject response) {
                    try {

                        String[] Longs = Utilities.jsonArrayToList((JSONArray) response.get("longs")).toArray(new String[0]);
                        String[] Lats = Utilities.jsonArrayToList((JSONArray) response.get("lats")).toArray(new String[0]);
                        String[] emails = Utilities.jsonArrayToList((JSONArray) response.get("emails")).toArray(new String[0]);
                        String[] lastUpdates = Utilities.jsonArrayToList((JSONArray) response.get("lastUpdated")).toArray(new String[0]);
                        ArrayList<Double> longsList = convertToDouble(Longs);
                        ArrayList<Double> latsList = convertToDouble(Lats);
                        Log.e("Hi", emails.length + "");
                        displayTrustedContactLoc(emails, latsList, longsList, lastUpdates); // TODO: Also get the route they are going on
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

    private ArrayList<Double> convertToDouble(String[] array) {
        ArrayList<Double> doubleList = new ArrayList<>();
        for(String numbers:array){
            doubleList.add(Double.valueOf(numbers));
        }
        return doubleList;
    }

    private void alertDialog(String map_loading_error, String message) {
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    public void enableLocationThings() {
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        //TODO: Make it zoom to current location
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
        else
            currentLocation = new LatLng(locationNet.getLatitude(), locationNet.getLongitude());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void displayTrustedContactLoc(
            String[] emails, ArrayList<Double> lats, ArrayList<Double> longs, String[] lastUpdated) {
        Marker mFriend;
        int online = 0;

        // Remove all the old ones first
        if (friendMarkers.size() > 0) {
            for (Marker m : friendMarkers)
                m.remove();
            friendMarkers.clear();
        }

        for (int i = 0; i < emails.length; i++) {
            Log.e("fmytag", "" + i + ": " + emails[i] + "," + longs.get(i) + "," + lats.get(i) + "," + lastUpdated[i]);
            String oldLast = lastUpdated[i];
            String last = oldLast.substring(0,10)+'T'+oldLast.substring(11);
            int drawable;
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O)
                drawable = R.drawable.active_icon;
            else {
                LocalDateTime lastOnDate = LocalDateTime.parse(last);
                LocalDateTime minsAgo = LocalDateTime.now().minusMinutes(1);
                if (minsAgo.compareTo(lastOnDate) > 0)
                    drawable = R.drawable.offline_icon;
                else {
                    drawable = R.drawable.active_icon;
                    online++;
                }
            }

            mFriend = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(longs.get(i), lats.get(i)))
                    .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(drawable, "" + emails[i])))
                    .title(emails[i])
                    .anchor(0.5f, 1)
            );
            friendMarkers.add(mFriend);
            if (emails[i].equals(AccountInfo.getFriendFocusedOn()) && !isActive) {
                moveToCurrentLocation(longs.get(i), lats.get(i));
            }

            mFriend.setTag(1);
        }
        firstTime = false;
    }

    private void addTrustedContact(String email) {
        trustedContactNames.add(0, email);
        Log.e("Add", email);
        adapter.notifyItemInserted(0);
    }

    private Bitmap getMarkerBitmapFromView(@DrawableRes int resId, String username) {

        View customMarkerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.profile_icon_view, null);
        ImageView markerImageView = (ImageView) customMarkerView.findViewById(R.id.profile_image);
        markerImageView.setImageResource(resId);
        customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());
        customMarkerView.buildDrawingCache();

        TextView customTextView = (TextView) customMarkerView.findViewById(R.id.text_view);
        customTextView.setText(String.valueOf(username.toUpperCase().charAt(0)));

        Bitmap returnedBitmap = Bitmap.createBitmap(customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);

        Drawable drawable = customMarkerView.getBackground();
        if (drawable != null)
            drawable.draw(canvas);
        customMarkerView.draw(canvas);
        return returnedBitmap;
    }

    private void moveToCurrentLocation(double lats, double longs )
    {
        LatLng currentLocation =  new LatLng(lats, longs);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation,15f));
    }

    public void sendGetFriendRoutePOST(String email,String friendEmail) {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = getResources().getString(R.string.server_ip) + "getFriendRoute";

        try {
            JSONObject jsonBody = new JSONObject();

            jsonBody.put("email", email);
            jsonBody.put("friendEmail", friendEmail);
            Log.e("hm", friendEmail);
            // Put your headers here

            JsonObjectRequest jsonObject = new JsonObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    showRoute(response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                }
            });
            // Add the request to the RequestQueue.
            queue.add(jsonObject);

        } catch (
                JSONException e) {
        }
    }

    private void showRoute(JSONObject response){
        try {
            String encodedRoute = (String) response.get("route");
            Log.e("Agg", encodedRoute);

            List<LatLng> points = decodePolyLine(encodedRoute);
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

    private List<LatLng> decodePolyLine(String routePoints) {
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
            decoded.add(point);
        }

        return decoded;
    }

    @Override
    public void onItemClick(View view, int position) {
        AccountInfo.setFriendFocusedOn(trustedContactNames.get(position));
        Toast.makeText(this, "Now focusing on: "+ trustedContactNames.get(position),
                Toast.LENGTH_SHORT).show();
        focusFriend.setText(getString(R.string.watching,
                trustedContactNames.get(position)));
        changeActive(false);
    }
}
