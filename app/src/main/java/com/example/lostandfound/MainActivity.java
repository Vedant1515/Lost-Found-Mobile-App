package com.example.lostandfound;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Lost & Found");
        }

        findViewById(R.id.btnCreateAdvert).setOnClickListener(v ->
                startActivity(new Intent(this, CreateAdvertActivity.class)));

        findViewById(R.id.btnShowItems).setOnClickListener(v ->
                startActivity(new Intent(this, ItemListActivity.class)));

        findViewById(R.id.btnShowOnMap).setOnClickListener(v ->
                startActivity(new Intent(this, MapActivity.class)));
    }
}
