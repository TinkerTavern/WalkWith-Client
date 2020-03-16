package com.example.walkwith;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

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

        Button removeTC = findViewById(R.id.removeTCButton);

        removeTC.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                onTCButtonClick(view);
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

        //Toast.makeText(this, "You deleted " + adapter.getItem(position) + " on row number " + position, Toast.LENGTH_SHORT).show();
        nameIndex = position;
        //trustedContactNames.remove(position);
        //adapter.notifyItemRemoved(position);
        Intent openInfo = new Intent(this, ContactInfo.class);
        startActivity(openInfo);
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

    public void onTCButtonClick(View view){
        removeSingleItem();
    }

    private void removeSingleItem(){ // Changed this so it removes the first value all the time
        // I think I see what the intention was here, but realistically the removal should be done in
        // The contactInfo activity
//        openList();
        if (trustedContactNames.size() > 0) {
            Toast.makeText(this, "You deleted " + adapter.getItem(0) +
                    " on row number " + 0, Toast.LENGTH_SHORT).show();
            trustedContactNames.remove(0);
            adapter.notifyItemRemoved(0);

        }
        else
            Toast.makeText(this, "Nothing to remove", Toast.LENGTH_SHORT).show();

    }

    protected void openList(){
        Intent openList = new Intent(this, TrustedContactList.class);
        startActivity(openList);
    }
}
