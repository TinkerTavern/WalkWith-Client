package com.example.walkwith;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;

public class TrustedContactList extends AppCompatActivity implements MyRecyclerViewAdapter.ItemClickListener{
    MyRecyclerViewAdapter adapter;

    ArrayList<String> trustedContactNames = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trusted_contact_list);

        Button addTC = findViewById(R.id.button);

        addTC.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                onButtonClick(view);
            }
        });



        //data to populate the RecyclerView with
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

    public void onButtonClick(View view){
        insertSingleItem();
    }

    private void insertSingleItem(){
        String item = "Glasses";
        int insertIndex = 0;
        trustedContactNames.add(insertIndex, item);
        adapter.notifyItemInserted(insertIndex);
    }
}
