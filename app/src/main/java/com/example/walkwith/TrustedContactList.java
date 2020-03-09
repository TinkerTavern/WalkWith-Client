package com.example.walkwith;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

public class TrustedContactList extends AppCompatActivity implements MyRecyclerViewAdapter.ItemClickListener{
    MyRecyclerViewAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trusted_contact_list);

        //data to populate the RecyclerView with
        ArrayList<String> trustedContactNames = new ArrayList<>();
        trustedContactNames.add("Tom");
        trustedContactNames.add("Elora");
        trustedContactNames.add("Matt");
        trustedContactNames.add("Matas");
        trustedContactNames.add("Jerry");
        trustedContactNames.add("Zhivko");

        RecyclerView recyclerView = findViewById(R.id.TrustedContacts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyRecyclerViewAdapter(this, trustedContactNames);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);

    }

    @Override
    public void onItemClick(View view, int position) {
        Toast.makeText(this, "You clicked " + adapter.getItem(position) + " on row number " + position, Toast.LENGTH_SHORT).show();
    }
}
