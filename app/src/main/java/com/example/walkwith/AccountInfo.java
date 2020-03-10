package com.example.walkwith;

import java.util.ArrayList;
import java.util.List;

public class AccountInfo {
    private String username;
    private String email;
    private List<String> friendsList;
    private String friendFocusedOn;

    AccountInfo(String username, String email){
        this.username = username;
        this.email = email;
        friendsList = new ArrayList<String>();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getFriendsList() {
        return friendsList;
    }

    public String[] convertToArray(List<String> list){
        String[] newList = new String[list.size()];
        list.toArray(newList);
        return newList;
    }

    public String getFriendFocusedOn() {
        return friendFocusedOn;
    }

    public void setFriendFocusedOn(String friendFocusedOn) {
        this.friendFocusedOn = friendFocusedOn;
    }
}
