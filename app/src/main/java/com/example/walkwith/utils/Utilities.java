package com.example.walkwith.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.walkwith.AccountInfo;
import com.example.walkwith.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class Utilities {

    public static ArrayList<Object> jsonArrayToList(JSONArray arr) {
        ArrayList<Object> newArr = new ArrayList<>();
        try {
            for (int i = 0; i < arr.length(); i++) {
                newArr.add(arr.get(i));
            }
        } catch (JSONException e) {
            e.getMessage(); //
        }
        return newArr;
    }

    public static void updateTrustedContacts(Context parentContext) {
        RequestQueue queue = Volley.newRequestQueue((parentContext));
        String url = parentContext.getResources().getString(R.string.server_ip) + "getTrustedContacts";
        try {
            JSONObject jsonBody = new JSONObject();

            jsonBody.put("email", AccountInfo.getEmail());

            JsonObjectRequest jsonObject = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                    new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        String[] emails = Utilities.jsonArrayToList((JSONArray)
                                response.get("emails")).toArray(new String[0]);
/*                        for (String a : emails) {
                            Log.d("test", a);
                            Toast.makeText(parentContext, a, Toast.LENGTH_LONG).show();

                        }*/
                        AccountInfo.setFriendsList(emails);
                    }
                    catch (JSONException e) {
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

    public static ArrayList<String> listToArrayList(String[] list) {
        return new ArrayList<>(Arrays.asList(list));
    }

    public static void simpleMessage(Activity parentActivity, final String title, final String message) {
        parentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(parentActivity.getApplicationContext())
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
        });
    }

}
