package com.example.cs160_sp18.prog3;

public class Landmark {
    public String landmark_name;
    public String coordinates;
    public String filename;

    Landmark(String landmarkName, String coordinates, String fileName){
        this.landmark_name = landmarkName;
        this.coordinates = coordinates;
        this.filename = fileName;
    }
}
