package com.example.cs160_sp18.prog3;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;

// Adapter for the recycler view in CommentFeedActivity. You do not need to modify this file
public class LandmarkAdapter extends RecyclerView.Adapter {

    private Context mContext;
    private ArrayList<Landmark> mLandmarks;
    private double latitude;
    private double longitude;
    private Intent loginIntent;

    public LandmarkAdapter(Context context, ArrayList<Landmark> landmarks, double latitude, double longitude, Intent intent) {
        mContext = context;
        mLandmarks = landmarks;
        this.latitude = latitude;
        this.longitude = longitude;
        this.loginIntent = intent;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.card_layout, parent, false);
        return new LandmarkViewHolder(view, loginIntent);
    }


    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        Landmark landmark = mLandmarks.get(position);
        ((LandmarkViewHolder) holder).bind(landmark,latitude, longitude);
    }

    @Override
    public int getItemCount() {
        return mLandmarks.size();
    }
}

class LandmarkViewHolder extends RecyclerView.ViewHolder {

    public CardView mCardLayout;
    public ImageView mLandmarkImageView;
    public TextView mLandmarkTitle;
    public TextView mLandmarkDistance;
    private Intent loginIntent;

    public LandmarkViewHolder(final View itemView, Intent intent) {
        super(itemView);
        mCardLayout = itemView.findViewById(R.id.card_view);
        mLandmarkImageView = mCardLayout.findViewById(R.id.card_view_image);
        mLandmarkTitle = mCardLayout.findViewById(R.id.card_view_image_title);
        mLandmarkDistance = mCardLayout.findViewById(R.id.card_view_image_distance);
        this.loginIntent = intent;

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String strDistance = mLandmarkDistance.getText().toString();
                if(Double.parseDouble(strDistance.split("m")[0]) < 10.0){
                    String username = loginIntent.getStringExtra("username");
                    Intent commentIntent = new Intent(itemView.getContext(), CommentFeedActivity.class);
                    commentIntent.putExtra("username",username);
                    commentIntent.putExtra("landmarkName",mLandmarkTitle.getText().toString());
                    itemView.getContext().startActivity(commentIntent);
                }
                else{
                    Toast.makeText(itemView.getContext(), "You need to be no more than 10 meters away to access comment feed for landmark.",
                            Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    void bind(Landmark landmark, double latitude, double longitude) {
        Uri imageUri = Uri.parse("android.resource://com.example.paigeplander.cs160_prog3/drawable/" + landmark.filename);
        mLandmarkImageView.setImageURI(imageUri);
        mLandmarkTitle.setText(landmark.landmark_name);
        mLandmarkDistance.setText(coordinatesToDistance(landmark.coordinates, latitude, longitude));
    }


    public String coordinatesToDistance(String coordinates, double latitude, double longitude){
        String returnDistance = "";
        String landmarkCoordinates = coordinates.replace(" ","");
        String[] coords = landmarkCoordinates.split(",");
        double landmarkLatitude = Double.parseDouble(coords[0]);
        double landmarkLongitude = Double.parseDouble(coords[1]);
        double distanceKM = haversine(landmarkLatitude,landmarkLongitude,latitude,longitude);
        returnDistance = String.format(Locale.US, "%.2f", distanceKM*1000);
        return returnDistance+"m away";
    }

    public static final double earthRadius = 6372.8; // In kilometers
    public static double haversine(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double a = Math.pow(Math.sin(dLat / 2),2) + Math.pow(Math.sin(dLon / 2),2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return earthRadius * c;
    }
}

