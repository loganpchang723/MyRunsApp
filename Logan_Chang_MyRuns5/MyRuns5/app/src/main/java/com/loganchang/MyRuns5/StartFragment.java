package com.loganchang.MyRuns5;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;

import java.util.HashMap;
import java.util.Map;


public class StartFragment extends Fragment implements View.OnClickListener{
    //widgets + intent
    private Button mStartButton, mSyncButton;
    private Spinner mInputSpinner, mActivitySpinner;
    private Intent mIntent;

    //input types
    public static final String MANUAL_ENTRY = "Manual Entry";
    public static final String AUTOMATIC = "Automatic";
    public static final String GPS = "GPS";
    public static final Map<String,Integer> INPUT_TO_ID_MAP;
    static{
        INPUT_TO_ID_MAP = new HashMap<>();
        INPUT_TO_ID_MAP.put(MANUAL_ENTRY,0);
        INPUT_TO_ID_MAP.put(AUTOMATIC,1);
        INPUT_TO_ID_MAP.put(GPS,2);
    }
    public static final String[] INPUT_TO_ID = {"Manual Entry", "Automatic", "GPS"};
    public static final Map<String,Integer> ACTIVITY_TO_ID_MAP;
    static{
        ACTIVITY_TO_ID_MAP = new HashMap<>();
        ACTIVITY_TO_ID_MAP.put("Running", 0);
        ACTIVITY_TO_ID_MAP.put("Walking", 1);
        ACTIVITY_TO_ID_MAP.put("Standing", 2);
        ACTIVITY_TO_ID_MAP.put("Cycling", 3);
        ACTIVITY_TO_ID_MAP.put("Hiking", 4);
        ACTIVITY_TO_ID_MAP.put("Downhill Skiing", 5);
        ACTIVITY_TO_ID_MAP.put("Cross-Country Skiing", 6);
        ACTIVITY_TO_ID_MAP.put("Snowboarding", 7);
        ACTIVITY_TO_ID_MAP.put("Skating", 8);
        ACTIVITY_TO_ID_MAP.put("Swimming", 9);
        ACTIVITY_TO_ID_MAP.put("Mountain Biking", 10);
        ACTIVITY_TO_ID_MAP.put("Wheelchair", 11);
        ACTIVITY_TO_ID_MAP.put("Elliptical", 12);
        ACTIVITY_TO_ID_MAP.put("Other", 13);
        ACTIVITY_TO_ID_MAP.put("Unknown", 14);
    }
    public static final String[] ACTIVITY_TO_ID = {"Running", "Walking", "Standing",
            "Cycling", "Hiking", "Downhill Skiing", "Cross-Country Skiing", "Snowboarding",
            "Skating", "Swimming", "Mountain Biking", "Wheelchair", "Elliptical", "Other", "Unknown"};

    //intent keys
    public static final String INPUT_TYPE = "input_type";
    public static final String ACTIVITY_TYPE = "activity_type";



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //inflate layout
        View view = inflater.inflate(R.layout.fragment_start, container, false);
        //get buttons and spinners
        mStartButton = (Button) view.findViewById(R.id.start_start_button);
        mSyncButton = (Button) view.findViewById(R.id.start_sync_button);
        mInputSpinner = (Spinner) view.findViewById(R.id.start_input_spinner);
        mActivitySpinner = (Spinner) view.findViewById(R.id.start_activity_spinner);
        //bind click listener
        mStartButton.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        String inputType = mInputSpinner.getSelectedItem().toString();
        Log.d("input type", "input type: "+inputType);
        String activityType = mActivitySpinner.getSelectedItem().toString();
        //go to ManualEntryActivity if input is "Manual Entry", else go to MapActivity
        if(inputType.equals(MANUAL_ENTRY)) mIntent = new Intent(getActivity(), ManualEntryActivity.class);
        else mIntent = new Intent(getActivity(), MapDisplayActivity.class);
        mIntent.putExtra(INPUT_TYPE, INPUT_TO_ID_MAP.get(inputType));
        if(inputType.equals(AUTOMATIC)) mIntent.putExtra(ACTIVITY_TYPE, ACTIVITY_TO_ID_MAP.get("Unknown"));
        else mIntent.putExtra(ACTIVITY_TYPE, ACTIVITY_TO_ID_MAP.get(activityType));
        getActivity().startActivity(mIntent);
    }
}