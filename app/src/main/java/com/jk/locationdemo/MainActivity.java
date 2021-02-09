package com.jk.locationdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

//https://github.com/profjigisha/AndroidJavaLocationDemo.git

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnOpenMap;
    private Button btnShowNavigation;
    private TextView tvLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.btnOpenMap = findViewById(R.id.btnOpenMap);
        this.btnOpenMap.setOnClickListener(this);

        this.btnShowNavigation = findViewById(R.id.btnShowNavigation);
        this.btnShowNavigation.setOnClickListener(this);

        tvLocation = findViewById(R.id.tvLocation);
    }

    @Override
    public void onClick(View view) {
        if (view != null){
            switch (view.getId()){
                case R.id.btnOpenMap:

                    break;

                case R.id.btnShowNavigation:

                    break;
            }
        }
    }
}