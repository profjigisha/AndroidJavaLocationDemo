package com.jk.locationdemo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

/**
 * LocationDemo Created by jkp on 09/02/21.
 */
public class LocationManager {
    private final String TAG = this.getClass().getCanonicalName();
    private FusedLocationProviderClient fusedLocationProviderClient = null;
    Boolean locationPermissionGranted = false;
    final int LOCATION_PERMISSION_REQUEST_CODE = 101;
    private final String[] permissionArray = new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION};

    private LocationRequest locationRequest;
    MutableLiveData<Location> location = new MutableLiveData<>();

    private static final LocationManager ourInstance = new LocationManager();

    static LocationManager getInstance() {
        return ourInstance;
    }

    private LocationManager() {
        this.createLocationRequest();
    }

    private void createLocationRequest(){
        this.locationRequest = new LocationRequest();
        this.locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        this.locationRequest.setInterval(5000); //5 seconds
    }

    public void checkPermissions(Context context){
        this.locationPermissionGranted = (ContextCompat.checkSelfPermission(context.getApplicationContext(),
        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);

        Log.e(TAG, "LocationPermissionGranted " + this.locationPermissionGranted);

        if (!this.locationPermissionGranted) {
            this.requestLocationPermission(context);
        }
    }

    public void requestLocationPermission(Context context) {
        ActivityCompat.requestPermissions((Activity) context, this.permissionArray, this.LOCATION_PERMISSION_REQUEST_CODE);
    }

    public FusedLocationProviderClient getFusedLocationProviderClient(Context context) {
        if (fusedLocationProviderClient == null) {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        }

        return fusedLocationProviderClient;
    }

    @SuppressLint("MissingPermission")
    public MutableLiveData<Location> getLastLocation(Context context) {
        if (this.locationPermissionGranted) {
            try {

                this.getFusedLocationProviderClient(context)
                        .getLastLocation()
                        .addOnSuccessListener(new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location loc) {
                                if (loc != null) {
                                    location.setValue(loc);
                                    Log.e(TAG, "Last Location ---- Lat : " + location.getValue().getLatitude() +
                                            " Lng : " + location.getValue().getLongitude());
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, e.toString());
                                Log.e(TAG, e.getLocalizedMessage());
                            }
                        });

            } catch (Exception ex) {
                Log.e(TAG, ex.getLocalizedMessage());
                return null;
            }

            return this.location;

        } else {
            //explain the user why location is unavailable
            //request for permissions
        }

        return null;
    }
}
