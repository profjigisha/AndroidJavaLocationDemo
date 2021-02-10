package com.jk.locationdemo;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.android.PolyUtil;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.TravelMode;

import org.joda.time.DateTime;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private final String TAG = this.getClass().getCanonicalName();
    private final Float DEFAULT_ZOOM = 15.0f;
    private LocationManager locationManager;
    private LocationCallback locationCallback;
    private LatLng currentLocation;
    private static final int overview = 0;
    private LatLng destinationLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        this.locationManager = LocationManager.getInstance();
        this.locationManager.checkPermissions(this);

        if (this.locationManager.locationPermissionGranted) {

            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult == null) {
                        return;
                    }

                    for (Location loc : locationResult.getLocations()) {

                        currentLocation = new LatLng(loc.getLatitude(), loc.getLongitude());
//                        mMap.animateCamera(CameraUpdateFactory.newLatLng(currentLocation));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, DEFAULT_ZOOM));
//                        mMap.addMarker(new MarkerOptions().position(currentLocation).title("You're here !"));
                        showDirections();

                        Log.e(TAG, "Lat : " + loc.getLatitude() + "\nLng : " + loc.getLongitude());
                        Log.e(TAG, "Number of locations" + locationResult.getLocations().size());
                    }
                }
            };

            this.locationManager.requestLocationUpdates(this, locationCallback);
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
//        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        //Royal Ontario Museum, Toronto
        this.destinationLocation = new LatLng(43.671188, -79.485771);

        if (googleMap != null) {
            googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            this.setupGoogleMapScreenSetting(googleMap);

            mMap = googleMap;
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

    private void setupGoogleMapScreenSetting(GoogleMap gMap) {
        gMap.setBuildingsEnabled(true);
        gMap.setIndoorEnabled(true);
        gMap.setTrafficEnabled(false);

        UiSettings myUiSettings = gMap.getUiSettings();

        myUiSettings.setZoomControlsEnabled(true);
        myUiSettings.setZoomGesturesEnabled(true);
        myUiSettings.setMyLocationButtonEnabled(true);
        myUiSettings.setScrollGesturesEnabled(true);
        myUiSettings.setRotateGesturesEnabled(true);
    }

    private void showDirections() {
        String originAddress = this.getAddress(this.currentLocation);
        String destinationAddress = this.getAddress(this.destinationLocation);
        Log.e(TAG, "originAddress : " + originAddress);
        Log.e(TAG, "destinationAddress : " + destinationAddress);

        DirectionsResult results = getDirectionsDetails(originAddress, destinationAddress, TravelMode.DRIVING);

        if (results != null) {
            Log.e(TAG, "Results : " + results.routes.length);
            addPolyline(results, mMap);
            positionCamera(results.routes[overview], mMap);
            addMarkersToMap(results, mMap);
        } else {
            Log.e(TAG, "No direction results found");
        }
    }

    private String getAddress(LatLng coordinates) {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(coordinates.latitude, coordinates.longitude, 1);
            // Here 1 represent max location result to returned, by documents it recommended 1 to 5

            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            String postalCode = addresses.get(0).getPostalCode();
            String knownName = addresses.get(0).getFeatureName();

            Log.e(TAG, "complete address : " + address);

            return address;
        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }

        return null;
    }

    private DirectionsResult getDirectionsDetails(String originAddress, String destinationAddress, TravelMode mode) {
        DateTime now = new DateTime();
        try {
            return DirectionsApi.newRequest(getGeoContext())
                    .mode(mode)
                    .origin(originAddress)
                    .destination(destinationAddress)
                    .departureTime(now)
                    .await();
        } catch (ApiException e) {
            e.printStackTrace();
            return null;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void addPolyline(DirectionsResult results, GoogleMap mMap) {
        List<LatLng> decodedPath = PolyUtil.decode(results.routes[overview].overviewPolyline.getEncodedPath());

        Polyline routeLine = mMap.addPolyline(new PolylineOptions().addAll(decodedPath));
        routeLine.setStartCap(new RoundCap());
        routeLine.setEndCap(new RoundCap());
        routeLine.setWidth(50);
        routeLine.setColor(getColor(R.color.purple_500));
        routeLine.setJointType(JointType.ROUND);
    }

    private String getEndLocationTitle(DirectionsResult results) {
        return "Time :" + results.routes[overview].legs[overview].duration.humanReadable + " Distance :" + results.routes[overview].legs[overview].distance.humanReadable;
    }

    private GeoApiContext getGeoContext() {
        GeoApiContext geoApiContext = new GeoApiContext();
        return geoApiContext
                .setQueryRateLimit(3)
                .setConnectTimeout(1, TimeUnit.SECONDS)
                .setReadTimeout(1, TimeUnit.SECONDS)
                .setWriteTimeout(1, TimeUnit.SECONDS)
                .setApiKey(getResources().getString(R.string.mapsApiKey));
    }

    private void addMarkersToMap(DirectionsResult results, GoogleMap mMap) {
        mMap.addMarker(new MarkerOptions().position(new LatLng(results.routes[overview].legs[overview].startLocation.lat, results.routes[overview].legs[overview].startLocation.lng)).title(results.routes[overview].legs[overview].startAddress));
        mMap.addMarker(new MarkerOptions().position(new LatLng(results.routes[overview].legs[overview].endLocation.lat, results.routes[overview].legs[overview].endLocation.lng)).title(results.routes[overview].legs[overview].startAddress).snippet(getEndLocationTitle(results)));
    }

    private void positionCamera(DirectionsRoute route, GoogleMap mMap) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(route.legs[overview].startLocation.lat, route.legs[overview].startLocation.lng), 12));
    }
}