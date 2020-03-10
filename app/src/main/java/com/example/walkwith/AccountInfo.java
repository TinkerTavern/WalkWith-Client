package com.example.walkwith;

import java.util.ArrayList;

public class AccountInfo {
    private static String username;
    private static String email;
    private static ArrayList<String> friendsList;
    private static String friendFocusedOn;

    AccountInfo(/*String username,*/ String email, ArrayList<String> friendsList){
        //this.username = username;
        AccountInfo.email = email;
        AccountInfo.friendsList = friendsList;
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
