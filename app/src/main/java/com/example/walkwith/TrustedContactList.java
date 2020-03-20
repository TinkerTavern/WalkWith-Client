package com.example.walkwith;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
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

import java.util.ArrayList;


public class TrustedContactList extends AppCompatActivity implements MyRecyclerViewAdapter.ItemClickListener{
    MyRecyclerViewAdapter adapter;
    ArrayList<String> trustedContactNames = new ArrayList<>();
    int nameIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trusted_contact_list);

        Button addTC = findViewById(R.id.addTCButton);

        addTC.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                onButtonClick(view);
            }
        });

        //data to populate the RecyclerView with
        getTrustedContacts();

        RecyclerView recyclerView = findViewById(R.id.TrustedContacts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyRecyclerViewAdapter(this, trustedContactNames);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);

    }

    private void getTrustedContacts() {
        trustedContactNames.addAll(AccountInfo.getFriendsList());
    }

    @Override
    public void onItemClick(View view, int position) {
        nameIndex = position;
        String email = trustedContactNames.get(position);
        getUserInfoPOST(email);
    }

    private void getUserInfoPOST(String email) { // TODO Make this a general function
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = getResources().getString(R.string.server_ip) + "getFriendInfo";
        try {
            JSONObject jsonBody = new JSONObject();

            jsonBody.put("email", email);

            JsonObjectRequest jsonObject = new JsonObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        String result = (String) response.get("result");
                        if (result.equals("True")) {
                            String tcEmail = (String) response.get("email");
                            String firstName = (String) response.get("firstName");
                            String surname = (String) response.get("lastName");
                            String phoneNum = (String) response.get("phoneNum");
                            Intent openInfo = new Intent(getApplicationContext(), ContactInfo.class);
                            openInfo.putExtra("email", email);
                            openInfo.putExtra("firstName", firstName);
                            openInfo.putExtra("surname", surname);
                            openInfo.putExtra("phoneNum", phoneNum);
                            startActivity(openInfo);
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Error in retrieving " +
                                    "details, try again soon", Toast.LENGTH_SHORT).show();
                        }
                    }
                    catch (JSONException e) {
                        Toast.makeText(getApplicationContext(), "Error in retrieving " +
                                "details, try again soon", Toast.LENGTH_SHORT).show();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getApplicationContext(), "Error in retrieving " +
                            "details, try again soon", Toast.LENGTH_SHORT).show();
                }
            });
            queue.add(jsonObject);

        } catch (
                JSONException e) {
            e.printStackTrace();
        }
    }

    private void addContactPopup() {
        android.app.AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Contact");
        builder.setMessage("Enter the email of the contact you wish to add");
        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Add Contact", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                addContact(input.getText().toString());
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

    public void onButtonClick(View view){
        addContactPopup();
    }

    private void addContact(String name){
        String item = name;
        int insertIndex = 0;
        trustedContactNames.add(insertIndex, item);
        adapter.notifyItemInserted(insertIndex);
    }
}
