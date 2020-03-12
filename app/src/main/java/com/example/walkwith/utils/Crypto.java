package com.example.walkwith.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Crypto {

    private String hashString(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            byte[] digest = md.digest(value.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            return "Error";
        }
    }
}
