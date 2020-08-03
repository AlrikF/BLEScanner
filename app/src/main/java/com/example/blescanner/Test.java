package com.example.blescanner;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class Test extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        String data;
        data=getIntent().getStringExtra("Task");
        Toast.makeText(getApplicationContext(),data,Toast.LENGTH_SHORT).show();
    }
}
