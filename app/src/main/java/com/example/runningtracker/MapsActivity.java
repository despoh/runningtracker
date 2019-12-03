package com.example.runningtracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    RunningSession session;
    String[] locations;
    DbHelper dbHelper;
    PlacesClient placesClient;
    PlaceLikelihood likelihoodPlace;
    TextView totalTimeTextView ;
    TextView totalDistanceTextView ;
    TextView avgSpeedTextView ;
    TextView startTimeTextView ;
    TextView nearByPlaceTextView ;
    TextView dateTextView ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        populateTextView();
        Places.initialize(getApplicationContext(), "AIzaSyCeRAPzbvg-nIhjw5l0gusLGvUFith2lxY");
        placesClient = Places.createClient(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        dbHelper = new DbHelper(getApplicationContext(), null, null, 1);
        session =  dbHelper.findById(getIntent().getIntExtra("sessionId",-1));
        locations = session.getStringList().split(" \\+ ");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getNearbyLocation();
    }

    public void populateTextView(){
        totalTimeTextView = (TextView) findViewById(R.id.map_totalTimeTextView);
        totalDistanceTextView = (TextView) findViewById(R.id.map_totalDistanceTextView);
        avgSpeedTextView = (TextView) findViewById(R.id.map_avgSpeedTextView);
        startTimeTextView = (TextView) findViewById(R.id.map_startingTimeTextView);
        nearByPlaceTextView = (TextView) findViewById(R.id.map_placeTextView);
        dateTextView = (TextView) findViewById(R.id.map_dateTextView);
    }

    public void updateView(){
        SimpleDateFormat dateFormatter = new SimpleDateFormat("d MMM YYYY");
        SimpleDateFormat timeFormatter = new SimpleDateFormat("h:mm a");

        totalTimeTextView.setText(minuteSecondString((int)session.getTotalTime()*1000));
        totalDistanceTextView.setText(String.format("%.2f", session.getTotalDistance()) + " km");
        avgSpeedTextView.setText(String.format("%.2f", session.getAvgSpeed()) + " m/min");
        startTimeTextView.setText(timeFormatter.format(new Date(session.getDate())));
        dateTextView.setText(dateFormatter.format(new Date(session.getDate())));
        nearByPlaceTextView.setText("Location: near " + likelihoodPlace.getPlace().getName());

    }

    public String minuteSecondString(int millsec){
        int hours = (millsec/1000)/60/60;
        int minutes = (millsec/1000) /60;
        int second = (millsec/1000) % 60;

        if(hours == 0 && minutes == 0){
            return second + " s";
        }else if(hours == 0){
            return minutes + "m " + second + " s";
        }else{
            return hours + " h " + minutes + " m " + second + " s";
        }
    }

    public void getNearbyLocation(){
        List<Place.Field> placeFields = Collections.singletonList(Place.Field.NAME);

        FindCurrentPlaceRequest request = FindCurrentPlaceRequest.newInstance(placeFields);

        Task<FindCurrentPlaceResponse> placeResponse = placesClient.findCurrentPlace(request);
        placeResponse.addOnCompleteListener(new OnCompleteListener<FindCurrentPlaceResponse>() {
            @Override
            public void onComplete(@NonNull Task<FindCurrentPlaceResponse> task) {
                if (task.isSuccessful()){
                    FindCurrentPlaceResponse response = task.getResult();

                    likelihoodPlace = response.getPlaceLikelihoods().get(0);
                    updateView();

                } else {
                    Exception exception = task.getException();
                    if (exception instanceof ApiException) {
                        ApiException apiException = (ApiException) exception;
                        Log.d("mama", "Place not found: " + apiException.getStatusCode());
                    }
                }
            }
    });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){

            case android.R.id.home: onBackPressed();

            default: return super.onOptionsItemSelected(item);

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
        mMap = googleMap;
        LatLng startingPoint = null;
        LatLng endingPoint = null;
        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.RED);
        polylineOptions.width(5);
        for(int i=0;i< locations.length;i++){
            String[] locationArray = locations[i].split(",");
            if(i==0 ){
                startingPoint = new LatLng(Double.parseDouble(locationArray[0]),Double.parseDouble(locationArray[1]));
                if (locations.length == 1){
                    endingPoint = new LatLng(Double.parseDouble(locationArray[0]),Double.parseDouble(locationArray[1]));
                }
            }else if(i== locations.length -1){
                endingPoint = new LatLng(Double.parseDouble(locationArray[0]),Double.parseDouble(locationArray[1]));
            }
            polylineOptions.add(new LatLng(Double.parseDouble(locationArray[0]),Double.parseDouble(locationArray[1])));

        }


        mMap.addPolyline(polylineOptions);
        mMap.addMarker(new MarkerOptions().position(startingPoint).title("Start point"));
        mMap.addMarker(new MarkerOptions().position(endingPoint).title("End point"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startingPoint,14));
        googleMap.animateCamera(CameraUpdateFactory.zoomIn());
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(14), 2000, null);
    }
}
