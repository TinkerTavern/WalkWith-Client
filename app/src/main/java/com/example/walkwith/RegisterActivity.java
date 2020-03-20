package com.example.walkwith;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.walkwith.utils.Utilities;

import org.json.JSONException;
import org.json.JSONObject;

public class RegisterActivity extends AppCompatActivity {

    private EditText firstNameEditText;
    private EditText lastNameEditText;
    private EditText emailEditText;
    private EditText confirmEmailEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private EditText phoneNumberEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firstNameEditText = findViewById(R.id.first_name);
        lastNameEditText = findViewById(R.id.last_name);
        emailEditText = findViewById(R.id.email);
        confirmEmailEditText = findViewById(R.id.confirm_email);
        passwordEditText = findViewById(R.id.password);
        confirmPasswordEditText = findViewById(R.id.confirm_password);
        phoneNumberEditText = findViewById(R.id.phone_number);

        final Button registerButton = findViewById(R.id.register);
        final Button gotAccountButton = findViewById(R.id.gotAccount);

        registerButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                termsAndConditions();
            }
        });

        gotAccountButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void validateInfo() {
        // Check for stuff like emails with @ etc.
        if (emailEditText.getText().toString().equals(confirmEmailEditText.getText().toString()))
            // Emails same
            if (passwordEditText.getText().toString().equals(
                    confirmPasswordEditText.getText().toString()))
                // Passwords same
                sendRegisterRequest(firstNameEditText.getText().toString(), lastNameEditText.getText().toString(), emailEditText.getText().toString(),
                passwordEditText.getText().toString(), phoneNumberEditText.getText().toString());
            else
                Toast.makeText(this, "Passwords don't match", Toast.LENGTH_SHORT)
                        .show();
        else
            Toast.makeText(this, "Emails don't match", Toast.LENGTH_SHORT).show();

    }

    private void sendRegisterRequest(String firstName, String lastName, String email, String password, String phone) {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = getResources().getString(R.string.server_ip) + "account";


        try {
            JSONObject jsonBody = new JSONObject();

            jsonBody.put("mode", "register");
            jsonBody.put("firstName", firstName);
            jsonBody.put("lastName", lastName);
            jsonBody.put("email", email);
            jsonBody.put("password", password);
            jsonBody.put("phoneNum", phone);
            // Put your headers here

            JsonObjectRequest jsonObject = new JsonObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        validateLogin((String) response.get("result"), email);
                        // Put the things you want to happen upon success
                    } catch (JSONException e) {
                        validateLogin("JSON Error - " + e.getMessage(), email);
                        // Put the error here
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // put the error here
                    validateLogin("Server Error - " + error.getMessage(), email);
                }
            });
            // Add the request to the RequestQueue.
            queue.add(jsonObject);

        } catch (
                JSONException e) {
            validateLogin("Error - " + e.getMessage(), email);
        }
    }

    private void validateLogin(String response, String email) {
        if (response.equals("True")) {
            Utilities.updateTrustedContacts(this);
            new AccountInfo(email);
            Toast.makeText(this, "Welcome, " + emailEditText.getText().toString(), Toast.LENGTH_SHORT).show();
            Intent myIntent = new Intent(RegisterActivity.this, MainMenu.class);
            finish();
            RegisterActivity.this.startActivity(myIntent);
        }
        else
            Toast.makeText(this, response, Toast.LENGTH_SHORT).show();

    }

    private void termsAndConditions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Terms and Conditions");
        builder.setMessage("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Curabitur mauris sem, tempus ut bibendum nec, maximus non risus. Integer iaculis est justo, sed pharetra elit volutpat a. Nunc cursus enim vel metus maximus facilisis. Cras eros sapien, congue id porttitor nec, egestas sit amet nunc. Mauris id sem tellus. Donec laoreet, nulla sit amet lacinia eleifend, dolor nulla rutrum eros, ac rutrum metus nisl sit amet arcu. Duis luctus viverra sem, eu ultricies lectus sodales ut. Duis vehicula volutpat neque et semper. Donec malesuada vel ante ac aliquet. Etiam et dolor sed justo scelerisque commodo sed vitae velit. Mauris leo felis, suscipit eu lacinia eget, suscipit sit amet nisl. Donec accumsan elit nec suscipit efficitur. Ut non lobortis tortor. Nam et tempus diam, ac vehicula sem. Morbi mauris mauris, accumsan eu urna ac, tempus interdum urna.\n" +
                "\n" +
                "Interdum et malesuada fames ac ante ipsum primis in faucibus. Nullam convallis sapien ut elit posuere, sed egestas velit sodales. Suspendisse hendrerit varius lorem, eu vehicula odio mattis a. Vestibulum leo sem, iaculis ut dapibus et, tristique id nunc. In hac habitasse platea dictumst. Donec et interdum neque, vitae ullamcorper metus. Etiam non erat sagittis, faucibus libero eu, commodo erat. Aenean vel magna ac tellus venenatis facilisis ut non turpis. Aenean tempor sed magna maximus consequat.\n" +
                "\n" +
                "Sed lobortis porttitor nulla, et sagittis ligula interdum sed. Etiam at mattis quam. Pellentesque molestie eros vitae arcu mollis, eget luctus urna mattis. Ut sed erat risus. Integer sagittis condimentum luctus. Aenean ligula dolor, aliquam sodales ex eu, lacinia iaculis lacus. Vivamus vitae mi eu nibh gravida tincidunt.\n" +
                "\n" +
                "Ut varius nulla eu facilisis convallis. Vestibulum mattis consectetur convallis. Suspendisse non tellus sodales, ullamcorper quam ac, cursus tellus. Ut molestie massa ut rutrum mollis. Suspendisse potenti. Integer maximus, neque at efficitur mattis, erat erat eleifend ex, sed pulvinar sem ligula quis leo. Pellentesque sodales vestibulum volutpat. Aenean faucibus consectetur varius. Donec vel tempor quam. Suspendisse ligula ex, scelerisque at cursus eu, facilisis ac massa. Suspendisse potenti. Phasellus pharetra lacus a risus aliquam elementum. Mauris quis metus posuere, tempor neque sit amet, lacinia lorem. Quisque eget velit varius, pulvinar dui et, tempor metus.\n" +
                "\n" +
                "Donec vitae risus ut augue sodales facilisis iaculis sollicitudin sapien. Donec lobortis ipsum accumsan velit fermentum, malesuada ornare dolor pretium. Vestibulum arcu leo, commodo sed pellentesque ut, pellentesque ac risus. Quisque sed sem id erat congue ornare. Curabitur sodales metus eu faucibus placerat. Nunc eleifend consequat eros, id lobortis arcu vehicula id. Etiam rhoncus metus dui, nec semper urna tincidunt a.");
        // Set up the buttons
        builder.setPositiveButton("Agree", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                validateInfo();
            }
        });
        builder.setNegativeButton("Disagree", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(), "T&C's declined, app closing...", Toast.LENGTH_SHORT).show();
                dialog.cancel();
                finishAffinity();
            }
        });
        builder.show();
    }
}
