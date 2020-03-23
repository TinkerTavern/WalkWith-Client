package com.example.walkwith;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class EditProfileActivity extends AppCompatActivity {

    private EditText forename, surname, email, phone, password, confirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        SharedPreferences preferences = getSharedPreferences("checkbox", MODE_PRIVATE);
        String theme = preferences.getString("theme","");
        if (!theme.equals(""))
            AppCompatDelegate.setDefaultNightMode(Integer.parseInt(theme));
        forename = findViewById(R.id.firstnameVal);
        surname = findViewById(R.id.surnameVal);
        email = findViewById(R.id.emailVal);
        phone = findViewById(R.id.phoneNumVal);
        password = findViewById(R.id.passwordVal);
        confirmPassword = findViewById(R.id.confirmPasswordVal);

        Button deleteAcc = findViewById(R.id.deleteAccount);
        Button editAcc = findViewById(R.id.editProfile);
        Button confirm = findViewById(R.id.confirmChanges);
        Button cancel = findViewById(R.id.cancel);

        deleteAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passwordPopup();
            }
        });

        editAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteAcc.setVisibility(View.GONE);
                editAcc.setVisibility(View.GONE);
                cancel.setVisibility(View.VISIBLE);
                confirm.setVisibility(View.VISIBLE);
                setEnabled(true);
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateDetails();
                deleteAcc.setVisibility(View.VISIBLE);
                editAcc.setVisibility(View.VISIBLE);
                cancel.setVisibility(View.GONE);
                confirm.setVisibility(View.GONE);
                setEnabled(false);
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getUserInfo();
                deleteAcc.setVisibility(View.VISIBLE);
                editAcc.setVisibility(View.VISIBLE);
                cancel.setVisibility(View.GONE);
                confirm.setVisibility(View.GONE);
                setEnabled(false);
            }
        });

        getUserInfo();
    }

    private void setEnabled(boolean stats) {
        forename.setEnabled(stats);
        surname.setEnabled(stats);
        email.setEnabled(stats);
        phone.setEnabled(stats);
        password.setEnabled(stats);
        confirmPassword.setEnabled(stats);
    }

    private void updateDetails() {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = getResources().getString(R.string.server_ip) + "account";

        try {
            JSONObject jsonBody = new JSONObject();

            String userEmail = AccountInfo.getEmail();

            if (userEmail == null) {
                SharedPreferences preferences = getSharedPreferences("checkbox", MODE_PRIVATE);
                String newEmail = preferences.getString("email", "");
                if (!newEmail.equals(""))
                    userEmail = newEmail;
                else {
                    Toast.makeText(this, "lost email?", Toast.LENGTH_SHORT).show();
                }
            }
            jsonBody.put("email", userEmail);
            jsonBody.put("mode", "updateInfo");
            jsonBody.put("password", password.getText().toString());
            if (confirmPassword.getText().toString().length() > 0 && !confirmPassword.getText().toString().equals(password.getText().toString()))
                jsonBody.put("newPass", confirmPassword.getText().toString());
            else
                jsonBody.put("newPass", password.getText().toString());
            jsonBody.put("firstName", forename.getText().toString());
            jsonBody.put("lastName", surname.getText().toString());
            jsonBody.put("phoneNum", phone.getText().toString());

            JsonObjectRequest jsonObject = new JsonObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Toast.makeText(getApplicationContext(), "Details changed successfully",
                            Toast.LENGTH_SHORT).show();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // put the error here
                    Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            // Add the request to the RequestQueue.
            queue.add(jsonObject);

        } catch (
                JSONException e) {
            Toast.makeText(getApplicationContext(), "Routing issues", Toast.LENGTH_SHORT).show();
        }
    }

    private void getUserInfo() {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = getResources().getString(R.string.server_ip) + "getFriendInfo";

        try {
            JSONObject jsonBody = new JSONObject();

            String userEmail = AccountInfo.getEmail();
            if (userEmail == null) {
                SharedPreferences preferences = getSharedPreferences("checkbox", MODE_PRIVATE);
                String newEmail = preferences.getString("email", "");
                if (!newEmail.equals(""))
                    userEmail = newEmail;
                else {
                    Toast.makeText(this, "lost email?", Toast.LENGTH_SHORT).show();
                }
            }
            jsonBody.put("email", userEmail);

            JsonObjectRequest jsonObject = new JsonObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    setupInfo(response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // put the error here
                    Toast.makeText(getApplicationContext(), "Routing issues", Toast.LENGTH_SHORT).show();
                }
            });
            // Add the request to the RequestQueue.
            queue.add(jsonObject);

        } catch (
                JSONException e) {
            Toast.makeText(getApplicationContext(), "Routing issues", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupInfo(JSONObject response) {
        try {
            String result = (String) response.get("result");
            if (!result.equals("True"))
                Toast.makeText(getApplicationContext(), result,
                        Toast.LENGTH_SHORT).show();
            else {
                email.setText((String) response.get("email"));
                forename.setText((String) response.get("firstName"));
                surname.setText((String) response.get("lastName"));
                phone.setText((String) response.get("phoneNum"));
            }
        }
        catch (JSONException e) {
            Toast.makeText(getApplicationContext(), e.getMessage(),
                    Toast.LENGTH_SHORT).show();

        }
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
