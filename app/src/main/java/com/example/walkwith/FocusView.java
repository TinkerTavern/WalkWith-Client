package com.example.walkwith;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class FocusView extends FragmentActivity implements GoogleMap.OnMyLocationClickListener, GoogleMap.OnMyLocationButtonClickListener, OnMapReadyCallback {
    private GoogleMap mMap;

    private AccountInfo accountInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_focus_view);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        try {
            Objects.requireNonNull(mapFragment).getMapAsync(this);
        }
        catch (NullPointerException e) {
            alertDialog("Map loading error", e.getMessage());
        }
    }

    public void onMapReady(GoogleMap map){
        mMap = map;
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);

        //sendPOST("Focus", accountInfo.getEmail(), new String[]{accountInfo.getFriendFocusedOn()});

        //This is for testing use line up top
        //displayFocusedLoc(new String[]{accountInfo.getFriendFocusedOn()}, new int[]{52}, new double[]{2.1});
        displayFocusedLoc(new String[]{"a", "b", "c"}, new int[]{52, 32, 76}, new double[]{2.1, 3.2, 4.3});
        //moveToCurrentLocation(52,2.1);
    }

    private void sendPOST(String mode, String email, final String [] watchEmails) {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = getResources().getString(R.string.server_ip) + "account";
        try {
            JSONObject jsonBody = new JSONObject();

            jsonBody.put("email", email);
            jsonBody.put("viewingMode", mode);
            jsonBody.put("watchEmails", watchEmails);

            JsonObjectRequest jsonObject = new JsonObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        double[] Longs = (double[]) response.get("longs");
                        int[] Lats = (int[]) response.get("lats");
                        displayFocusedLoc(watchEmails, Lats, Longs);
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

    private void displayFocusedLoc(String[] emails, int[] lats, double[] longs) {
        Marker mFriend;

        for(int i = 0; i < emails.length; i++){
            mFriend = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(lats[i], longs[i]))
                    .title(emails[i])
            );
            mFriend.setTag(0);
        }
    }

    private void moveToCurrentLocation(int lats, double longs )
    {
        LatLng currentLocation =  new LatLng(lats, longs);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation,15));
        // Zoom in, animating the camera.
        mMap.animateCamera(CameraUpdateFactory.zoomIn());
        // Zoom out to zoom level 10, animating with a duration of 2 seconds.
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);

    }

    private void alertDialog(String map_loading_error, String message) {
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
