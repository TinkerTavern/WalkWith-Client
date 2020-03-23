package com.example.walkwith;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.walkwith.utils.Utilities;

import org.json.JSONException;
import org.json.JSONObject;

public class ContactInfo extends AppCompatActivity {

    private TextView emailVal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_info);
        setLabels();
        SharedPreferences preferences = getSharedPreferences("checkbox", MODE_PRIVATE);
        String theme = preferences.getString("theme","");
        if (!theme.equals(""))
            AppCompatDelegate.setDefaultNightMode(Integer.parseInt(theme));
        final Button removeContact = findViewById(R.id.removeTC);
        removeContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeContactPopup();
            }
        });
    }

    private void setLabels() {
        emailVal = findViewById(R.id.emailVal);
        final TextView firstnameVal = findViewById(R.id.firstnameVal);
        final TextView surnameVal = findViewById(R.id.surnameVal);
        final TextView phoneNumVal = findViewById(R.id.phoneNumVal);
        emailVal.setText(getIntent().getStringExtra("email"));
        firstnameVal.setText(getIntent().getStringExtra("firstName"));
        surnameVal.setText(getIntent().getStringExtra("surname"));
        phoneNumVal.setText(getIntent().getStringExtra("phoneNum"));
    }

    private void removeContactPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Deletion");
        builder.setMessage("Are you sure you want to remove the contact?");
        // Set up the buttons
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                removeContactPOST();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                Toast.makeText(getApplicationContext(), "Delete cancelled", Toast.LENGTH_SHORT).show();
            }
        });
        builder.show();
    }

    private void removeContactPOST() {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = getResources().getString(R.string.server_ip) + "trustedContacts";

        try {
            JSONObject jsonBody = new JSONObject();

            jsonBody.put("trustee", AccountInfo.getEmail());
            jsonBody.put("email", emailVal.getText().toString());
            jsonBody.put("mode", "remove");

            JsonObjectRequest jsonObject = new JsonObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        String result = (String) response.get("result");
                        if (result.equals("True")) {
                            Utilities.updateTrustedContacts(getApplicationContext());
                            Toast.makeText(getApplicationContext(), "Contact removed",
                                    Toast.LENGTH_SHORT).show();
                            // Should update to remove old one
                            Intent returnIntent = new Intent();
                            setResult(5, returnIntent);
                            finish();
                        }
                        else
                            Toast.makeText(getApplicationContext(), result,
                                    Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        Toast.makeText(getApplicationContext(), "Error in removing contact, " +
                                "please try again soon", Toast.LENGTH_SHORT).show();

                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // put the error here
                    Toast.makeText(getApplicationContext(), "Error in deleting contact, please try again soon", Toast.LENGTH_SHORT).show();
                }
            });
            // Add the request to the RequestQueue.
            queue.add(jsonObject);

        } catch (
                JSONException e) {
            Toast.makeText(getApplicationContext(), "Error in deleting contact, please try again soon", Toast.LENGTH_SHORT).show();
        }
    }
}
