package com.example.cs160_sp18.prog3;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;

import java.io.Console;
import java.io.FileReader;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;


public class LandmarkActivity extends AppCompatActivity {

    Toolbar mToolbar;
    private RecyclerView recList;
    private RecyclerView.Adapter mAdapter;
    private ArrayList<Landmark> mLandmarks = new ArrayList<Landmark>();
    ConstraintLayout layout;
    Intent landmarkIntent;

    private LocationRequest mLocationRequest;

    private long UPDATE_INTERVAL = 1000;
    private long FASTEST_INTERVAL = 2000; /* 2 sec */

    private double currentLatitude;
    private double currentLongitude;

    /*
    Part 2:
    TODO: Fix Refresh issue. Hook up to Firebase, BONUS features- email login w/ Firebase authentication, upvoting/downvoting comments (stored on FB)
     */

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landmark_activity);

        landmarkIntent = getIntent();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
            checkPermission();
        }

        startLocationUpdates();
        SetLandmarks landmarkTask = new SetLandmarks();
        landmarkTask.execute();

        layout = (ConstraintLayout) findViewById(R.id.landmark_layout);

        recList = (RecyclerView) findViewById(R.id.cardList);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

    }

    private class SetLandmarks extends AsyncTask<String, String, String>{

        @Override
        protected String doInBackground(String... params){
            try{
                String jsonString = loadJSONFromAsset();
                //JSONObject obj = new JSONObject(jsonString);
                JSONArray arr = new JSONArray(jsonString);
                for(int i = 0; i<arr.length(); i++){
                    JSONObject temp = arr.getJSONObject(i);
                    Landmark nextLandmark = new Landmark((String) temp.get("landmark_name"),(String) temp.get("coordinates"),(String) temp.get("filename"));
                    mLandmarks.add(nextLandmark);
                }
            }
            catch (JSONException e){
                //some error handling
                e.printStackTrace();
            }
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            setAdapterAndUpdateData();
        }


        @Override
        protected void onPreExecute() {
        }


        @Override
        protected void onProgressUpdate(String... text) {
        }

    }

    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = getAssets().open("bear_statues.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
            json = json.replace("\n", "").replace("\r", "");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    private void setAdapterAndUpdateData() {
        mAdapter = new LandmarkAdapter(this, mLandmarks, currentLatitude, currentLongitude, landmarkIntent);
        recList.setAdapter(mAdapter);
        //recList.smoothScrollToPosition(mLandmarks.size() - 1);
    }

    public void refreshLocation(View view){
        getLastLocation(true);
    }

    protected void startLocationUpdates() {

        // Create the location request to start receiving updates
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        //mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        try{
            getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            onLocationChanged(locationResult.getLastLocation(),false);
                        }
                    },
                    Looper.myLooper());
        }
        catch(SecurityException e){
            e.printStackTrace();
        }

    }

    public void onLocationChanged(Location location, boolean calledFromButton) {
        // New location has now been determined
        if (location != null) {
            currentLatitude = location.getLatitude();
            currentLongitude = location.getLongitude();
            if (calledFromButton){
                SetLandmarks landmarkTask = new SetLandmarks();
                landmarkTask.execute();
            }
        }

    }

    public void getLastLocation(final boolean calledFromButton) {
        // Get last known recent location using new Google Play Services SDK (v11+)
        FusedLocationProviderClient locationClient = getFusedLocationProviderClient(this);

        try{
            locationClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            onLocationChanged(location, calledFromButton);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("LOCATION FAILURE", "Error trying to get last GPS location");
                            e.printStackTrace();
                        }
                    });
        }
        catch (SecurityException e){
            e.printStackTrace();
        }
    }


    public void checkPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ){//Can add more as per requirement

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},
                    123);
        }
    }

}
