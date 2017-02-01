package com.newind.tinyserver;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.newind.Application;


public class MainActivity extends AppCompatActivity {
    Application application;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
