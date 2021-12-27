package com.loganchang.MyRuns5;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.loader.content.AsyncTaskLoader;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;


import java.util.ArrayList;

public class MapDisplayActivity extends FragmentActivity implements OnMapReadyCallback, ServiceConnection {

    //service
    private Intent serviceIntent;
    private boolean mIsBound;

    //broadcast
    public static final String ACTION = "action";
    public static final String BROADCAST_KEY = "broadcast key";
    public static final int FIRST_LOC = 1;
    public static final int UPDATE_LOC = 2;
    public static final int UPDATE_ACTIVITY = 3;
    private UpdateReceiver mUpdateReceiver;

    //activity updates
    private int runCount, walkCount, standCount;
    public static final String RUN_COUNT = "run count";
    public static final String WALK_COUNT = "walk count";
    public static final String STAND_COUNT = "stand count";


    //map
    private GoogleMap mMap;
    private Marker mStartLoc, mEndLoc;
    private float mCurrSpeed;
    private int currActivity;
    public final static String CURR_SPEED = "current speed";
    private CameraPosition mCameraPosition;
    public static final String CAMERA_POS_KEY = "camera pos key";
    private boolean mIsCentered;
    public static final String CENTERED_KEY = "centered key";


    //location service
    private TrackingService trackingService;

    //gps for current location
    public static final int PERMISSION_REQUEST_CODE = 0;

    //variables for from history
    public final static String NOT_DRAWN = "not drawn";
    private boolean fromHistory, notDrawn, newRun, isAutomatic;
    private int inputType;
    private int activityType;
    public static long entryID;

    //DB stuff
    private ExerciseEntryDbHelper mDBHelper;
    private ExerciseEntry mExerciseEntry;

    //permission grants
    public static final String PERMISSION_KEY = "permission key";
    public boolean permissionGranted = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_display);

        Bundle bundle;
        //set bundle to saved instance state if exists
        if (savedInstanceState != null) {
            bundle = savedInstanceState;
            permissionGranted = savedInstanceState.getBoolean(PERMISSION_KEY);
            mCameraPosition = savedInstanceState.getParcelable(CAMERA_POS_KEY);
            mIsCentered = savedInstanceState.getBoolean(CENTERED_KEY, false);
        } else {
            //bundle is from the intent
            bundle = getIntent().getExtras();
            newRun = true;
        }

        checkPermission();

        //instantiate the db helper
        if (MainActivity.DBhelper != null) mDBHelper = new ExerciseEntryDbHelper(this);

        //setup Map
        setUpMap();
        Log.d("LPC", "onCreate: permissionGranted = " + permissionGranted);

        //retrieve activity type counts
        runCount = bundle.getInt(RUN_COUNT, 0);
        walkCount = bundle.getInt(WALK_COUNT, 0);
        standCount = bundle.getInt(STAND_COUNT, 0);


        //determine if from history, and if so, what its entry id is
        fromHistory = bundle.getBoolean(HistoryFragment.FROM_HISTORY, false);
        if (fromHistory) entryID = bundle.getLong(HistoryFragment.ENTRY_ID, 0);
        notDrawn = bundle.getBoolean(NOT_DRAWN, true);

        //not from history -> start the service
        if (!fromHistory) {
            //get input type and activity type
            Button deleteButton = (Button) findViewById(R.id.delete_map_button);
            deleteButton.setVisibility(View.GONE);
            inputType = bundle.getInt(StartFragment.INPUT_TYPE, -1);
            if (StartFragment.INPUT_TO_ID[inputType].equals(StartFragment.AUTOMATIC))
                isAutomatic = true;
            activityType = bundle.getInt(StartFragment.ACTIVITY_TYPE, -1);
            if (permissionGranted) {
                //start service
                startService();
                //start broadcast receiver
                mUpdateReceiver = new UpdateReceiver();
                //bind service
                mIsBound = false;
                this.bindService();
            }
        }
    }


    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(HistoryFragment.FROM_HISTORY, fromHistory);
        outState.putLong(HistoryFragment.ENTRY_ID, entryID);
        outState.putBoolean(NOT_DRAWN, notDrawn);
        outState.putInt(StartFragment.INPUT_TYPE, inputType);
        outState.putInt(StartFragment.ACTIVITY_TYPE, activityType);
        outState.putBoolean(PERMISSION_KEY, permissionGranted);
        mCameraPosition = mMap.getCameraPosition();
        outState.putParcelable(CAMERA_POS_KEY, mCameraPosition);
        outState.putBoolean(CENTERED_KEY, mIsCentered);

        //save activity type counts
        outState.putInt(RUN_COUNT, runCount);
        outState.putInt(WALK_COUNT, walkCount);
        outState.putInt(STAND_COUNT, standCount);
    }

    public void onResume() {
        super.onResume();
        //safety check the db helper and map
        if (mDBHelper == null) mDBHelper = new ExerciseEntryDbHelper(this);
        setUpMap();
        if (!fromHistory) {
            //register receiver
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ACTION);
            registerReceiver(mUpdateReceiver, intentFilter);
        }
    }

    //*****INIT PROCESSES******//

    /**
     * Set up the map
     */
    public void setUpMap() {
        if (mMap == null) {
            SupportMapFragment mapFragment = ((SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map));
            mapFragment.setRetainInstance(true);
            mapFragment.getMapAsync(this);
        }
    }

    /**
     * Instantiate map and put a pin at (0,0)
     */
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //pin africa
//        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Africa"));
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        if(mCameraPosition!=null) {
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
        }
        if (fromHistory) {
            initHistory();
        }
//
    }

    /**
     * Initialize showing a history entry
     */
    public void initHistory() {
        Button saveButton = (Button) findViewById(R.id.save_map_button);
        Button cancelButton = (Button) findViewById(R.id.cancel_map_button);
        saveButton.setVisibility(View.GONE);
        cancelButton.setVisibility(View.GONE);
        //fetch entry
        mExerciseEntry = new LoaderThread(this).loadInBackground();
        drawHistory();
        updateMetrics();
    }

    /**
     * Start location and notification service
     */
    public void startService() {
        serviceIntent = new Intent(this, TrackingService.class);
        //pass the intent that started this activity to the service
        Bundle bundle = getIntent().getExtras();
        serviceIntent.putExtras(bundle);
        this.startService(serviceIntent);
    }


    /**
     * Bind the service
     */
    public void bindService() {
        //bind the service
        getApplicationContext().bindService(this.serviceIntent, this, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    /**
     * Get service reference and its entry
     */
    public void onServiceConnected(ComponentName name, IBinder service) {
        //instantiate the location service
        trackingService = ((TrackingService.LocationBinder) service).getReference();
        if (newRun) {
            mExerciseEntry = trackingService.getmExerciseEntry();
        }
    }


    //*******UPDATE PROCESSES*******//

    /**
     * Broadcast Receiver to get update requests from service
     */
    public class UpdateReceiver extends BroadcastReceiver {
        /**
         * Get the update entry, finish the last of the current stats, trace the map
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            if (trackingService != null) {
                //get which type of location update (first on not first) this is
                //and curr speed
                int broadcastType = intent.getIntExtra(BROADCAST_KEY, 0);
                //check if an activity type update
                if(broadcastType == UPDATE_ACTIVITY){
                    currActivity = intent.getIntExtra(StartFragment.ACTIVITY_TYPE, 0);
                    if(currActivity == 0) runCount++;
                    else if (currActivity == 1) walkCount++;
                    else if (currActivity == 2) standCount++;
                }


                mCurrSpeed = intent.getFloatExtra(CURR_SPEED, 0);
                //retrieve the entry
                mExerciseEntry = trackingService.getmExerciseEntry();
                //update trace and stats
                updateMap(broadcastType);
            }
        }
    }

    /**
     * Draw the new trace on the map and update the current stats
     */
    public void updateMap(int whichLoc) {
        ArrayList<LatLng> latLngs;
        //UPDATE MAP
        if(mExerciseEntry != null) {
            synchronized (latLngs = mExerciseEntry.getmLatLngs()) {
                //if first point
                if (whichLoc == FIRST_LOC && latLngs.size() == 1) {
                    Log.d("LPC", "updateMap: first point");
                    //set input and activity type
                    Bundle bundle = getIntent().getExtras();
                    int input = bundle.getInt(StartFragment.INPUT_TYPE);
                    Log.d("input", "input type: " + input);
                    //if gps mode, set the activity type too
                    mExerciseEntry.setmInputType(input);
                    if (!isAutomatic) mExerciseEntry.setmActivityType(activityType);
                    //set the current activity based on majority
//                else{
//                    Log.d("LPC", "run, walk, stand (update map): "+runCount+", "+walkCount+", "+standCount);
////                    int maxActivity = Math.max(Math.max(runCount, walkCount), standCount);
////                    if(maxActivity == runCount) currActivity = 0;
////                    else if(maxActivity == walkCount) currActivity = 1;
////                    else currActivity = 2;
////                    mExerciseEntry.setmActivityType(currActivity);
//                }
                    //start drawing the trace

                    //make a green start marker and start location
                    mStartLoc = mMap.addMarker(new MarkerOptions().position(latLngs.get(0)).
                            icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                    //make a red marker of current/end location
                    mEndLoc = mMap.addMarker(new MarkerOptions().position(latLngs.get(latLngs.size() - 1)).
                            icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                    //zoom
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLngs.get(0), 17));
                    mIsCentered = true;
                } else if (whichLoc == UPDATE_LOC && latLngs.size() > 1) {
                    //redraw start marker
                    if (mStartLoc != null) mStartLoc.remove();
                    //make a green start marker and start location
                    mStartLoc = mMap.addMarker(new MarkerOptions().position(latLngs.get(1)).
                            icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                    //create a polyline
                    PolylineOptions polylineOptions = new PolylineOptions();
                    polylineOptions.color(Color.BLACK);
                    //edge case
                    ArrayList<LatLng> temp = new ArrayList<>(latLngs);
                    temp.remove(0);
                    polylineOptions.addAll(temp);
                    //draw the polyline
                    mMap.addPolyline(polylineOptions);
                    //redraw end marker
                    if (mEndLoc != null) mEndLoc.remove();
                    mEndLoc = mMap.addMarker(new MarkerOptions().position(latLngs.get(latLngs.size() - 1)).
                            icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                    //animate map so start and end locs are always viewable
                    liveZoom();
                }
            }
            updateMetrics();
        }

    }

    /**
     * Handles live zoom
     */
    public void liveZoom() {
        //code was adapted from here (many thanks to user):
        //https://stackoverflow.com/questions/14828217/android-map-v2-zoom-to-show-all-the-markers

        //create a boundary of start and end locs
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(mStartLoc.getPosition());
        builder.include(mEndLoc.getPosition());
        LatLngBounds bounds = builder.build();
        //keep boundaries within middle 90% of screen
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        int padding = (int) (width * 0.05);
        //apply boundaries and update camera
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
        mMap.animateCamera(cu);
    }

    /**
     * Update the viewable metrics in the map
     */
    public void updateMetrics() {
        //get preferred distance unit
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String unitPref = pref.getString(getString(R.string.unit_preference), getString(R.string.unit_miles));
        //update input type text view
        TextView gpsTV = (TextView) findViewById(R.id.gps_type);
        //if automatic, set the activity type based off majority
        if(isAutomatic){
//            Log.d("LPC", "run, walk, stand (update metrics): "+runCount+", "+walkCount+", "+standCount);
            int maxActivity = Math.max(Math.max(runCount, walkCount), standCount);
            if(maxActivity == runCount) currActivity = 0;
            else if(maxActivity == walkCount) currActivity = 1;
            else  currActivity = 2;
            mExerciseEntry.setmActivityType(currActivity);
        }
        gpsTV.setText("Type: " + (StartFragment.ACTIVITY_TO_ID[mExerciseEntry.getmActivityType()]));
        //update avg speed text view
        TextView avgSpeedTV = (TextView) findViewById(R.id.gps_avg_speed);
        avgSpeedTV.setText("Avg Speed: " + formatSpeed(mExerciseEntry.getmAvgSpeed(), unitPref));
        //update curr speed text view
        TextView currSpeedTV = (TextView) findViewById(R.id.gps_cur_speed);
        if (fromHistory) currSpeedTV.setText("Curr Speed: n/a");
        else currSpeedTV.setText("Curr Speed: " + formatSpeed(mCurrSpeed, unitPref));
        //update climb text view
        TextView climbTV = (TextView) findViewById(R.id.gps_climb);
        climbTV.setText("Climb: " + String.format("%.2f", mExerciseEntry.getmClimb()) + " " + unitPref);
        //update calorie text view
        TextView calorieTV = (TextView) findViewById(R.id.gps_calories);
        calorieTV.setText("Calories: " + mExerciseEntry.getmCalorie() + " cal");
        //update distance text view
        TextView distanceTV = (TextView) findViewById(R.id.gps_distance);
        distanceTV.setText("Distance: " + formatDistance(mExerciseEntry.getmDistance(), unitPref));
    }

    //****FORMATTING FUNCTIONS FOR STATS****//
    public String formatSpeed(float speed, String unitPref) {
        String units = "mi/h";
        if (unitPref.equals("Kilometers")) {
            speed /= 0.621371; // converts from km to miles
            units = "km/hr";
        }
        return String.format("%.2f", speed) + " " + units;
    }

    public String formatDistance(float speed, String unitPref) {
        if (unitPref.equals("Kilometers")) {
            speed /= 0.621371; // converts from km to miles
        }
        return String.format("%.2f", speed) + " " + unitPref;
    }

    //*****BREAKDOWN PROCESSES*****//
    public void onPause() {
        //if live:
        if (!fromHistory) {
            //save the current elapse time
            if (mExerciseEntry != null)
                mExerciseEntry.setmDuration(trackingService.getmElapsedTime());
            //remove the starting and ending pins
            if (mStartLoc != null) mStartLoc.remove();
            if (mEndLoc != null) mEndLoc.remove();
            //unregister the receiver
            try {
                unregisterReceiver(mUpdateReceiver);
                this.unbindService();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
        //close db helper
        if (mDBHelper != null) mDBHelper.close();
        super.onPause();
    }

    /**
     * Unbinds the service
     */
    public void unbindService() {
        //unbind if currently bound
        if (mIsBound) {
            getApplicationContext().unbindService(this);
            mIsBound = false;
        }
    }

    /**
     * Handle service disconnection locally
     */
    public void onServiceDisconnected(ComponentName name) {
        //null out location service instance
        trackingService = null;
    }


    /**
     * Send a broadcast to service to stop the notification and stop the service
     */
    private void stopService() {
        if (!fromHistory) {
            if (trackingService != null) {
                //destroy notification and service
                Intent intent = new Intent();
                intent.setAction(TrackingService.ACTION);
                intent.putExtra(TrackingService.STOP_BROADCAST_KEY, TrackingService.STOP_BROADCAST);
                sendBroadcast(intent);
                this.unbindService();
                stopService(serviceIntent);

                Log.d("LPC", "service destroyed");
            }
        }
    }

    public void onDestroy() {
        if (isFinishing()) stopService();
        super.onDestroy();
    }


    //******FROM HISTORY******//

    /**
     * Draw the map trace from a history entry
     */
    public void drawHistory() {
        ArrayList<LatLng> latLngs = mExerciseEntry.getmLatLngs();
        Log.d("LPC", "drawHistory: latlngs size " + latLngs.size());

        //start loc
        Log.d("LPC", "drawHistory: is mMap null? " + (mMap == null));
        mStartLoc = mMap.addMarker(new MarkerOptions().position(latLngs.get(0)).
                icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        //animate if not drawn
        if (!mIsCentered) {
            // Zoom in
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLngs.get(0), 17));
            mIsCentered = true;
        }

        // polyline
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.BLACK);
        polylineOptions.addAll(latLngs);
        mMap.addPolyline(polylineOptions);
        // end marker
        mEndLoc = mMap.addMarker(new MarkerOptions().position(latLngs.get(latLngs.size() - 1))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

    }

    //*******DELETE BUTTON*******//

    /**
     * Start a DeleterThread to delete chosen DB entry
     */
    public void onDeleteClick(View view) {
        if (HistoryFragment.adapter.getCount() > 0) {
            DeleteThread deleteThread = new DeleteThread();
            deleteThread.start();
        }
        this.finish();
    }

    //*******BOTTOM BUTTON CALLBACKS*******//

    /**
     * Save the current live exercise after getting some remaining data
     */
    public void onSaveClick(View view) {
        synchronized (mExerciseEntry.getmLatLngs()) {
            //remove the first point (buggy)
            if (mExerciseEntry.getmLatLngs() != null && mExerciseEntry.getmLatLngs().size() > 1) {
                mExerciseEntry.getmLatLngs().remove(0);
            }
            mExerciseEntry.setmDuration(trackingService.getmElapsedTime());
            //use SaverThread to save entry to DB
            SaveThread saveThread = new SaveThread(mExerciseEntry);
            saveThread.start();
        }
        stopService();
        if(trackingService != null && trackingService.getmExerciseEntry() != null)
            trackingService.setmExerciseEntry(null);
        this.finish();
    }

    /**
     * Stop service on cancel click
     */
    public void onCancelClick(View view) {
        stopService();
        if(trackingService != null && trackingService.getmExerciseEntry() != null)
            trackingService.setmExerciseEntry(null);
        Toast.makeText(this, getString(R.string.manual_cancel_msg),
                Toast.LENGTH_SHORT).show();
        this.finish();
    }

    /**
     * Stop service on back click
     */
    public void onBackPressed() {
        stopService();
        if(trackingService != null && trackingService.getmExerciseEntry() != null)
            trackingService.setmExerciseEntry(null);
        this.finish();
        super.onBackPressed();
    }


    //*******HELPER THREADS*******//

    /**
     * Worker thread to run deletion on
     */
    private class DeleteThread extends Thread {
        //runnable to update adapter and UI in history fragment and show toast
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                HistoryFragment.adapter.notifyDataSetChanged();
                Toast.makeText(getApplicationContext(), "Entry #" + entryID + " deleted.",
                        Toast.LENGTH_SHORT).show();
            }
        };
        Handler handler = new Handler(Looper.getMainLooper());

        //run the deletion on a worker thread
        public void run() {
            if (mDBHelper == null) mDBHelper = new ExerciseEntryDbHelper(getApplicationContext());
            mDBHelper.removeEntry(entryID);
            handler.post(runnable);
        }
    }


    /**
     * Thread to get the selected entry
     */
    private class LoaderThread extends AsyncTaskLoader<ExerciseEntry> {
        public LoaderThread(@NonNull Context context) {
            super(context);
        }

        public ExerciseEntry loadInBackground() {
            if (mDBHelper == null) mDBHelper = new ExerciseEntryDbHelper(getApplicationContext());
            return mDBHelper.fetchEntryByIndex(entryID);
        }
    }


    /**
     * Thread to save the current exercise
     */
    private class SaveThread extends Thread {
        //local instance vars
        ExerciseEntry eEntry;
        long id;

        /**
         * Construct a thread to write to db in background
         *
         * @param eEntry ExerciseEntry being inserted
         */
        public SaveThread(ExerciseEntry eEntry) {
            this.eEntry = eEntry;
        }

        /**
         * Tell the history fragment's adapter to update the adapter
         * Make toast saying the entry of given id was saved
         */
        private final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (HistoryFragment.adapter != null) HistoryFragment.adapter.notifyDataSetChanged();
                Toast.makeText(getApplicationContext(), "Entry #" + id + " saved.",
                        Toast.LENGTH_SHORT).show();
            }
        };

        Handler handler = new Handler(Looper.getMainLooper());

        /**
         * Insert a new ExerciseEntry and get its db ID
         */
        public void run() {
            Log.d("LPC", "is db helper null? "+ (mDBHelper == null));
            Log.d("LPC", "is entry null? "+ (eEntry == null));
            if (mDBHelper == null) mDBHelper = new ExerciseEntryDbHelper(getApplicationContext());
            id = mDBHelper.insertEntry(eEntry);
            handler.post(runnable);
        }
    }

    //PERMISSION REQUESTS FOR GPS
    public void checkPermission() {
        if (Build.VERSION.SDK_INT < 23) return;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_CODE);
        else permissionGranted = true;
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                permissionGranted = true;
                startService();
                mUpdateReceiver = new UpdateReceiver();
                Log.d("LPC", "update receiver created");

                //bind service
                mIsBound = false;
                this.bindService();
            } else {
                finish();
            }
        }
    }
}