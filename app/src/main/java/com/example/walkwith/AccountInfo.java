package com.example.walkwith;

import com.example.walkwith.utils.Utilities;

import java.util.ArrayList;

public class AccountInfo {
    private static String username;
    private static String email;
    private static ArrayList<String> friendsList;
    private static String friendFocusedOn;

    public AccountInfo(/*String username,*/ String email){
        //this.username = username;
        AccountInfo.email = email;
    }

    public static void setFriendsList(String[] emails) {
        AccountInfo.friendsList = Utilities.listToArrayList(emails);
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
