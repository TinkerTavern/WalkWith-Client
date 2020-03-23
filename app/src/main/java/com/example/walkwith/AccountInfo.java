package com.example.walkwith;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.walkwith.utils.Utilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AccountInfo {
    private static String username;
    private static String email;
    private static ArrayList<String> friendsList;
    private static ArrayList<String> contactsTrusted;
    private static String friendFocusedOn;

    public AccountInfo(/*String username,*/ String email){
        //this.username = username;
        AccountInfo.email = email;
    }

    public static void setFriendsList(String[] emails) {
        if (AccountInfo.friendsList != null)
            AccountInfo.friendsList.clear();
        AccountInfo.friendsList = Utilities.listToArrayList(emails);
    }

    public static void setContactsTrusted(String[] emails) {
        if (emails.length == 0)
            return;
        if (AccountInfo.contactsTrusted != null)
            AccountInfo.contactsTrusted.clear();
        AccountInfo.contactsTrusted = Utilities.listToArrayList(emails);
    }

    public static ArrayList<String> getContactsTrusted() {
        return contactsTrusted;
    }

    public static String[] updateContactsTrusted(Context parentContext) {
        RequestQueue queue = Volley.newRequestQueue((parentContext));
        String url = parentContext.getResources().getString(R.string.server_ip) + "getContactsTrusted";
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
                                AccountInfo.setContactsTrusted(emails);
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
        return new String[] {};
    }


    public static String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        AccountInfo.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        AccountInfo.username = username;
    }

    public static ArrayList<String> getFriendsList() {
        return friendsList;
    }

    public static String[] convertToArray(ArrayList<String> list){
        if(!list.isEmpty()){
            String[] newList = new String[list.size()];
            list.toArray(newList);
            return newList;
        }
        return new String[0];
    }

    public static String getFriendFocusedOn() {
        return friendFocusedOn;
    }

    public static void setFriendFocusedOn(String friendFocusedOn) {
        AccountInfo.friendFocusedOn = friendFocusedOn;
    }
}
