package com.example.walkwith;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        final Button logOutButton = findViewById(R.id.logOut);

        logOutButton.setOnClickListener(v -> {
            SharedPreferences preferences = getSharedPreferences("checkbox", MODE_PRIVATE);
            String isRemember = preferences.getString("remember","");
            Log.d("test", "here");
            if (isRemember.equals("true")) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("remember", "false");
                editor.putString("email", "");
                editor.apply();
                Log.d("test", "there");
            }
            Intent returnToLogin = new Intent(this, LoginActivity.class);
            startActivity(returnToLogin);
            finishAffinity(); // Closes all other activities
        });
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
//            setPreferencesFromResource(R.xml.root_preferences, rootKey);
        }
    }
}