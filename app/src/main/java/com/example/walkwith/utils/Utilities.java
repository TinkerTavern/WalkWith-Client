package com.example.walkwith.utils;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

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

}
