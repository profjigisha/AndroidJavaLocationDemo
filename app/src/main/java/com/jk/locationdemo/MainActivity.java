package com.jk.locationdemo;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.model.LatLng;

//https://github.com/profjigisha/AndroidJavaLocationDemo.git

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnOpenMap;
    private Button btnShowNavigation;
    private TextView tvLocation;
    private LocationManager locationManager;
    private final String TAG = this.getClass().getCanonicalName();
    private LatLng location;
    private LocationCallback locationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.btnOpenMap = findViewById(R.id.btnOpenMap);
        this.btnOpenMap.setOnClickListener(this);

        this.btnShowNavigation = findViewById(R.id.btnShowNavigation);
        this.btnShowNavigation.setOnClickListener(this);

        tvLocation = findViewById(R.id.tvLocation);

        this.locationManager = LocationManager.getInstance();
        this.locationManager.checkPermissions(this);

        if (this.locationManager.locationPermissionGranted) {
//            this.getLocation();

            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult == null) {
                        return;
                    }

                    for (Location loc : locationResult.getLocations()) {
                        tvLocation.setText("Lat : " + loc.getLatitude() + "\nLng : " + loc.getLongitude());
                        Log.e(TAG, "Lat : " + loc.getLatitude() + "\nLng : " + loc.getLongitude());
                        Log.e(TAG, "Number of locations" + locationResult.getLocations().size());
                    }
                }
            };

            this.locationManager.requestLocationUpdates(this, this.locationCallback);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.locationManager.stopLocationUpdates(this, this.locationCallback);
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.locationManager.requestLocationUpdates(this, this.locationCallback);
    }

    @Override
    public void onClick(View view) {
        if (view != null) {
            switch (view.getId()) {
                case R.id.btnOpenMap:
                    this.showLocationOnMap();
                    break;

                case R.id.btnShowNavigation:

                    break;
            }
        }
    }

    private void showLocationOnMap() {
        Intent mapIntent = new Intent(this, MapsActivity.class);

        startActivity(mapIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == this.locationManager.LOCATION_PERMISSION_REQUEST_CODE) {
            this.locationManager.locationPermissionGranted = (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED);

            if (this.locationManager.locationPermissionGranted) {
                //start receiving location and display that on screen
                Log.e(TAG, "LocationPermissionGranted " + this.locationManager.locationPermissionGranted);
            }
            return;
        }
    }

    private void getLocation() {
        this.locationManager.getLastLocation(this).observe(this, new Observer<Location>() {
            @Override
            public void onChanged(Location loc) {
                if (loc != null) {
                    tvLocation.setText("Lat : " + loc.getLatitude() + "\nLng : " + loc.getLongitude());

                    location = new LatLng(loc.getLatitude(), loc.getLongitude());
                }
            }
        });
    }
}