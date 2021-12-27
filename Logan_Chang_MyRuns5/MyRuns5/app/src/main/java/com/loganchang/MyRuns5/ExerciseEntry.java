package com.loganchang.MyRuns5;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Calendar;

public class ExerciseEntry {

    //instance vars taken from course webpage
    private long id;
    private int mInputType;  // Manual, GPS or automatic
    private int mActivityType;     // Running, cycling etc.
    private long mDateTime = Calendar.getInstance().getTimeInMillis();    // When does this entry happen
    private int mDuration;         // Exercise duration in seconds
    private float mDistance;      // Distance traveled. Either in meters or feet.
    private float mAvgPace;       // Average pace
    private float mAvgSpeed;     // Average speed
    private int mCalorie;        // Calories burnt
    private float mClimb;         // Climb. Either in meters or feet.
    private int mHeartRate;       // Heart rate
    private String mComment;       // Comments
    private ArrayList<LatLng> mLatLngs;

    /**
     * Construct a default Exercise Entry object
     */
    public ExerciseEntry() {
        mInputType = 0;
        mActivityType = 0;
        mLatLngs = new ArrayList<>();
    }

    //id getter and setter
    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    //inputType getter and setter
    public void setmInputType(int mInputType) {
        this.mInputType = mInputType;
    }

    public int getmInputType() {
        return mInputType;
    }

    //activityType getter and setter
    public void setmActivityType(int mActivityType) {
        this.mActivityType = mActivityType;
    }

    public int getmActivityType() {
        return mActivityType;
    }

    //dateTime getter and setter
    public void setmDateTime(long mDateTime) {
        this.mDateTime = mDateTime;
    }

    public long getmDateTime() {
        return this.mDateTime;
    }

    //duration getter and setter
    public void setmDuration(int mDuration) {
        this.mDuration = mDuration;
    }

    public int getmDuration() {
        return mDuration;
    }

    //distance getter and setter
    public void setmDistance(float mDistance) {
        this.mDistance = mDistance;
    }

    public float getmDistance() {
        return mDistance;
    }

    //avgPave getter and setter
    public void setmAvgPace(float mAvgPace) {
        this.mAvgPace = mAvgPace;
    }

    public float getmAvgPace() {
        return mAvgPace;
    }

    //avgSpeed getter and setter
    public void setmAvgSpeed(float mAvgSpeed) {
        this.mAvgSpeed = mAvgSpeed;
    }

    public float getmAvgSpeed() {
        return mAvgSpeed;
    }

    //calorie getter and setter
    public void setmCalorie(int mCalorie) {
        this.mCalorie = mCalorie;
    }

    public int getmCalorie() {
        return mCalorie;
    }

    //climb getter and setter
    public void setmClimb(float mClimb) {
        this.mClimb = mClimb;
    }

    public float getmClimb() {
        return mClimb;
    }

    //heartRate getter and setter
    public void setmHeartRate(int mHeartRate) {
        this.mHeartRate = mHeartRate;
    }

    public int getmHeartRate() {
        return mHeartRate;
    }

    //comment getter and setter
    public void setmComment(String mComment) {
        this.mComment = mComment;
    }

    public String getmComment() {
        return mComment;
    }

    //latlngs setter getter and add
    public ArrayList<LatLng> getmLatLngs() { return mLatLngs; }

    public void setmLatLngs(ArrayList<LatLng> mLatLngs) { this.mLatLngs = mLatLngs; }

    public void addLatLng(LatLng latLng) { mLatLngs.add(latLng);}

    //toString
    @Override
    public String toString() {
        return mActivityType + ": " + mActivityType + ", " + mDateTime;
    }
}
