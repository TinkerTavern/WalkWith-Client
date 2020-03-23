package com.example.walkwith;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        SharedPreferences preferences = getSharedPreferences("checkbox", MODE_PRIVATE);
        String theme = preferences.getString("theme","");
        if (!theme.equals(""))
            AppCompatDelegate.setDefaultNightMode(Integer.parseInt(theme));
        final Button logOutButton = findViewById(R.id.logOut);
        final Button deleteAccountButton = findViewById(R.id.deleteAccount);
        Button viewAcc = findViewById(R.id.viewProfile);
        Button darkMode = findViewById(R.id.darkMode);

        darkMode.setOnClickListener(v -> {
            switchTheme();
        });

        viewAcc.setOnClickListener(v -> {
            Intent myIntent = new Intent(this, EditProfileActivity.class);
            this.startActivity(myIntent);
        });

        logOutButton.setOnClickListener(v -> {
            String isRemember = preferences.getString("remember","");
            Log.d("test", "here");
            if (isRemember.equals("true")) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("remember", "false");
                editor.putString("email", "");
                editor.apply();
                Log.d("test", "there");
            }
            returnToLogin();
        });

        deleteAccountButton.setOnClickListener(v -> {
            passwordPopup();
        });
    }


    private void switchTheme() {
        String[] opts = {"Light", "Dark", ""}; // 3rd is determined by android version
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q)
            opts[2] = "System Default";
        else
            opts[2] = "Set By Battery Saver";

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick a color");
        builder.setItems(opts, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int mode = AppCompatDelegate.MODE_NIGHT_NO;
                switch (which) {
                    case 0:
                        mode = AppCompatDelegate.MODE_NIGHT_NO;
                        break;
                    case 1:
                        mode = AppCompatDelegate.MODE_NIGHT_YES;
                        break;
                    case 2:
                        if (opts[2].equals("System Default"))
                            mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                        else
                            mode = AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY;
                        break;
                }
                SharedPreferences preferences = getSharedPreferences("checkbox", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("theme", String.valueOf(mode));
                editor.apply();
                AppCompatDelegate.setDefaultNightMode(mode);
            }
        });
        builder.show();
    }

    private void returnToLogin() {
        SharedPreferences preferences = getSharedPreferences("checkbox", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("remember", "false");
        editor.putString("email", "");
        editor.apply();
        Intent returnToLogin = new Intent(this, LoginActivity.class);
        startActivity(returnToLogin);
        finishAffinity(); // Closes all other activities
    }

    private void passwordPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Deletion");
        builder.setMessage("Please enter your password below if you wish to delete your account");
        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteAccountPost("delete", AccountInfo.getEmail(), input.getText().toString());
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    public void deleteAccountPost(String mode, String email, String password) {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = getResources().getString(R.string.server_ip) + "account";
        try {
            JSONObject jsonBody = new JSONObject();

            jsonBody.put("mode", mode);
            jsonBody.put("email", email);
            jsonBody.put("password", password);

            JsonObjectRequest jsonObject = new JsonObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        String result = response.getString("result");
                        if (result.equals("True")) {
                            result = "Successful deletion, returning to login";
                            returnToLogin();
                        }
                        else
                            result = "Delete account was unsuccessful, please try again";
                        Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
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
}