package com.example.walkwith;

import androidx.annotation.DrawableRes;
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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
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
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
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
import java.util.Objects;

public class MainMenu extends FragmentActivity implements View.OnClickListener, GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener, TaskLoadedCallback,
        OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private GoogleMap mMap;
    private int MY_LOCATION_REQUEST_CODE = 1;
    private ArrayList<Marker> friendMarkers = new ArrayList<>();


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

        trustThread = new Thread() {
            public void run() {
                // do stuff
                while (true) {
                    updateViewPOST(AccountInfo.getEmail());
                    try {
                        Thread.sleep(Integer.parseInt(getResources().getString(R.string.idleTimer)));
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        };

        try {
            Objects.requireNonNull(mapFragment).getMapAsync(this);
        } catch (NullPointerException e) {
            Utilities.simpleMessage(this, "Map loading error", e.getMessage());
        }
        Button viewSettings = findViewById(R.id.settings); //Settings button
        Button viewTrustedContacts = findViewById(R.id.trustedContacts); //Trusted Contacts button
        Button viewActiveWalkers = findViewById(R.id.activeWalkers); //Active Walkers button
        Button startNewWalk = findViewById(R.id.startWalk); //New Walk button

        viewSettings.setOnClickListener(this);
        viewTrustedContacts.setOnClickListener(this);
        viewActiveWalkers.setOnClickListener(this);
        startNewWalk.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
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

    protected void openSettings() {
        Intent openSettings = new Intent(this, SettingsActivity.class);
        startActivity(openSettings);
    }

    protected void viewTrustedContacts() {
        Utilities.updateTrustedContacts(this);
        Intent openContacts = new Intent(this, TrustedContactList.class);
        startActivity(openContacts);
    }

    protected void viewActiveWalkers() {
        Intent viewWalkers = new Intent(this, FocusView.class);
        CameraPosition position = mMap.getCameraPosition();
        viewWalkers.putExtra("cameraLat", position.target.latitude);
        viewWalkers.putExtra("cameraLong", position.target.longitude);
        viewWalkers.putExtra("cameraZoom", position.zoom);
        startActivity(viewWalkers);
    }

    protected void openWalking() {
        Intent openWalking = new Intent(this, WalkingActivity.class);
        CameraPosition position = mMap.getCameraPosition();
        openWalking.putExtra("cameraLat", position.target.latitude);
        openWalking.putExtra("cameraLong", position.target.longitude);
        openWalking.putExtra("cameraZoom", position.zoom);
        startActivity(openWalking);
    }

    private void startThread() {
        if (!trustThread.isAlive())
            trustThread.start();

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
        startThread();
        // Get users last location to show on map
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
            checkIfLocationOn();
        else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_LOCATION_REQUEST_CODE);
        }

        mMap.setOnMarkerClickListener(this);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_LOCATION_REQUEST_CODE) {
            if (permissions.length == 1 &&
                    permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION) &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED)
                checkIfLocationOn();
            else
                Toast.makeText(this, "Location services not allowed, functionality reduced", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_SHORT).show();
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

    public static Boolean isLocationEnabled(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // This is new method provided in API 28
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (lm != null)
                return lm.isLocationEnabled();
            else
                return false;
        } else {
            // This is Deprecated in API 28
            int mode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE,
                    Settings.Secure.LOCATION_MODE_OFF);
            return (mode != Settings.Secure.LOCATION_MODE_OFF);
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

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (Objects.equals(marker.getTag(), 1))
            AccountInfo.setFriendFocusedOn(marker.getTitle());
        Intent focusView = new Intent(this, FocusView.class);
        CameraPosition position = mMap.getCameraPosition();
        focusView.putExtra("cameraLat", position.target.latitude);
        focusView.putExtra("cameraLong", position.target.longitude);
        focusView.putExtra("cameraZoom", position.zoom);
        startActivity(focusView);
        return false;
    }

    @Override
    public void onTaskDone(Object... values) {
        if (route != null)
            route.remove();
        route = mMap.addPolyline((PolylineOptions) values[0]);
    }

    private void updateViewPOST(String email) { // TODO Make this a general function
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = getResources().getString(R.string.server_ip) + "updateView";
        try {
            JSONObject jsonBody = new JSONObject();

            jsonBody.put("email", email);

            JsonObjectRequest jsonObject = new JsonObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {

                        String[] Longs = Utilities.jsonArrayToList((JSONArray) response.get("longs")).toArray(new String[0]);
                        String[] Lats = Utilities.jsonArrayToList((JSONArray) response.get("lats")).toArray(new String[0]);
                        String[] emails = Utilities.jsonArrayToList((JSONArray) response.get("emails")).toArray(new String[0]);
                        String[] lastUpdates = Utilities.jsonArrayToList((JSONArray) response.get("lastUpdated")).toArray(new String[0]);
                        ArrayList<Double> longsList = convertToDouble(Longs);
                        ArrayList<Double> latsList = convertToDouble(Lats);
                        displayTrustedContactLoc(emails, latsList, longsList, lastUpdates);

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
        for (String numbers : array) {
            doubleList.add(Double.valueOf(numbers));
        }
        return doubleList;
    }

    private void displayTrustedContactLoc(
            String[] emails, ArrayList<Double> lats, ArrayList<Double> longs, String[] lastUpdated) {
        Marker mFriend;
        // Remove all the old ones first
        if (friendMarkers.size() > 0) {
            for (Marker m : friendMarkers)
                m.remove();
            friendMarkers.clear();
        }

        for (int i = 0; i < emails.length; i++) {
            Log.e("mytag", "" + i + ": " + emails[i] + "," + longs.get(i) + "," + lats.get(i) + "," + lastUpdated[i]);
            String oldLast = lastUpdated[i];
            String last = oldLast.substring(0,10)+'T'+oldLast.substring(11);
            LocalDateTime lastOnDate = LocalDateTime.parse(last);
            LocalDateTime minsAgo = LocalDateTime.now().minusMinutes(1);
            int drawable;
            if (minsAgo.compareTo(lastOnDate) > 0)
                drawable = R.drawable.offline_icon;
            else
                drawable = R.drawable.active_icon;
            mFriend = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(longs.get(i), lats.get(i)))
                    .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(drawable, "" + emails[i])))
                    .title(emails[i])
                    .anchor(0.5f, 1)
            );
            friendMarkers.add(mFriend);
            mFriend.setTag(1);
        }
    }

    private Bitmap getMarkerBitmapFromView(@DrawableRes int resId, String username) {

        View customMarkerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.profile_icon_view, null);
        ImageView markerImageView = (ImageView) customMarkerView.findViewById(R.id.profile_image);
        markerImageView.setImageResource(resId);
        customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());
        customMarkerView.buildDrawingCache();

        TextView customTextView = (TextView) customMarkerView.findViewById(R.id.text_view);
        customTextView.setText(username);

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
}
