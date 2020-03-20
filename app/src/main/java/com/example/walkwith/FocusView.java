package com.example.walkwith;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.example.walkwith.MainMenu.isLocationEnabled;

public class FocusView extends FragmentActivity implements GoogleMap.OnMyLocationClickListener, GoogleMap.OnMyLocationButtonClickListener, OnMapReadyCallback {
    private GoogleMap mMap;
    private Polyline line;
    private PolylineOptions route;
    private LatLng friendLocation;
    private Double latitude, longitude, LATLONG_DEFAULT = 0d;
    private Float zoom, ZOOM_DEFAULT = 15f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_focus_view);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        zoom = getIntent().getFloatExtra("cameraZoom", ZOOM_DEFAULT);
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
                while (true) {
                    updateViewPOST(AccountInfo.getEmail());
                    try {
                        Thread.sleep(Integer.parseInt(getResources().getString(R.string.focusTimer)));
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
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        sendGetFriendRoutePOST(AccountInfo.getEmail(),AccountInfo.getFriendFocusedOn());
        if (!zoom.equals(ZOOM_DEFAULT) && !latitude.equals(LATLONG_DEFAULT) &&
                !longitude.equals(LATLONG_DEFAULT)) {
            LatLng latLng = new LatLng(latitude, longitude);
            CameraUpdate location = CameraUpdateFactory.newLatLngZoom(
                    latLng, zoom);
            mMap.animateCamera(location);
        }
        else
            Toast.makeText(getApplicationContext(), "Map consistency error",
                    Toast.LENGTH_SHORT).show();


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

    private void displayTrustedContactLoc(String[] emails, ArrayList<Double> lats, ArrayList<Double>
            longs, String[] lastUpdated){
        Marker mFriend;

        if (mMap == null)
            return;
        for(int i = 0; i < emails.length; i++){
            Log.e("mytag","" + i + ": " + emails[i] + "," + lats.get(i) + "," + longs.get(i) + "," + lastUpdated[i]);
            mFriend = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(longs.get(i), lats.get(i)))
                    .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(R.drawable.active_icon, "" + emails[i])))
                    .anchor(0.5f,1)
            );
            mFriend.setTag(i);
            if (emails[i].equals(AccountInfo.getFriendFocusedOn())) {
                moveToCurrentLocation(lats.get(i), longs.get(i));
            }
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

    private void moveToCurrentLocation(double lats, double longs )
    {
        LatLng currentLocation =  new LatLng(lats, longs);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation,15));
        // Zoom in, animating the camera.
        mMap.animateCamera(CameraUpdateFactory.zoomIn());
        // Zoom out to zoom level 10, animating with a duration of 2 seconds.
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);

    }

    public void sendGetFriendRoutePOST(String email,String friendEmail) {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = getResources().getString(R.string.server_ip) + "getFriendRoute";

        try {
            JSONObject jsonBody = new JSONObject();

            jsonBody.put("email", email);
            jsonBody.put("friendEmail", friendEmail);
            // Put your headers here

            JsonObjectRequest jsonObject = new JsonObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    showRoute(response);
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

    private void showRoute(JSONObject response){
        try {
            String encodedRoute = (String) response.get("route");

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
}
