package com.example.walkwith;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class ContactInfo extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_info);
        setLabels();
    }

    private void setLabels() {
        final TextView emailVal = findViewById(R.id.emailVal);
        final TextView firstnameVal = findViewById(R.id.firstnameVal);
        final TextView surnameVal = findViewById(R.id.surnameVal);
        final TextView phoneNumVal = findViewById(R.id.phoneNumVal);
        emailVal.setText(getIntent().getStringExtra("email"));
        firstnameVal.setText(getIntent().getStringExtra("firstName"));
        surnameVal.setText(getIntent().getStringExtra("surname"));
        phoneNumVal.setText(getIntent().getStringExtra("phoneNum"));
    }
}
