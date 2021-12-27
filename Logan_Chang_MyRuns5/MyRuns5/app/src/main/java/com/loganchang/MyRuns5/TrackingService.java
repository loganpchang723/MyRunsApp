package com.loganchang.MyRuns5;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;

public class TrackingService extends Service implements LocationListener, SensorEventListener {

    //exercise entry
    private ExerciseEntry mExerciseEntry;
    private ArrayList<LatLng> latLngs;
    private int mElapsedTime;
    private Timer mDurationTimer;

    //location
    LocationManager locationManager;
    private Location prevLocation;

    //notification
    public final static String ACTION = "notification action";
    public final static String STOP_BROADCAST_KEY = "stop broadcast";
    public final static int STOP_BROADCAST = 1;
    public static final String CHANNEL_ID = "channel id";
    public static final String CHANNEL_NAME = "MyRuns";
    public static final int NOTIFICATION_ID = 1;
    public static final String FROM_NOTIF = "from notif";

    //binder
    private final LocationBinder locationBinder = new LocationBinder();

    //activity sensor
    private static ArrayBlockingQueue<Double> mAccBuffer;
    private Sensor mAccelerometer;
    private final int BLOCK_SIZE = 64;
    private boolean isCancelled;
    private ActivityThread mActivityThread;

    //service notification receiver
    private ServiceNotificationReceiver serviceNotificationReceiver;


    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        serviceNotificationReceiver = new ServiceNotificationReceiver();
        mAccBuffer = new ArrayBlockingQueue<Double>(
                BLOCK_SIZE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("LPC", "onStartCommand: called");
        //set up location manager and its criteria
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setSpeedRequired(false);
        String provider = locationManager.getBestProvider(criteria, true);
        Log.d("LPC", "provider: " + provider);

        //set up accelerometer and sensor manager
        SensorManager mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        mSensorManager.registerListener(this, mAccelerometer,
                SensorManager.SENSOR_DELAY_FASTEST);

        boolean isFirst = true;


        //get most recent location
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> {
            try {
                Location location = locationManager.getLastKnownLocation(provider);
                startLocationUpdates(location, true);
                Log.d("LPC", "starting location updates for first location");
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        });

        //request location updates
        try {
            locationManager.requestLocationUpdates(provider, 0, 0, this);
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        //notification and its broadcast receiver for when it should kill
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION);
        registerReceiver(serviceNotificationReceiver, intentFilter);
        setUpNotification(intent);

        //set up the activity update thread
        mActivityThread = new ActivityThread();
        mActivityThread.start();


        //create exercise entry
        Log.d("LPC", "onStartCommand: is entry null: " + (mExerciseEntry == null));
        if (mExerciseEntry == null) {
            createExerciseEntry(intent, isFirst);
        }


        return super.onStartCommand(intent, flags, startId);
    }


    //******INIT PROCESSES******//

    /**
     * Create a notification channel
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Create a notification that will launch the map activity when clicked
     */
    public void setUpNotification(Intent originalIntent) {
        Context context = getApplicationContext();
        String notificationTitle = "MyRuns";
        String notificationText = "Recording your path now";

        Intent intent = new Intent(context, MapDisplayActivity.class);

        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setAction(Intent.ACTION_MAIN);
        Bundle bundle = originalIntent.getExtras();
        bundle.putBoolean(FROM_NOTIF, true);

        intent.putExtras(bundle);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.notification_logo)
                .setContentTitle(notificationTitle)
                .setContentText(notificationText)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        notificationManager.notify(NOTIFICATION_ID, builder.build());

        Log.d("LPC", "setUpNotification: built");
    }

    /**
     * Creates the exercise entry for the new run
     */
    public void createExerciseEntry(Intent intent, boolean isNew) {
        //set up the rest of the attributes, dependent on a new entry or not
        mExerciseEntry = new ExerciseEntry();
        mExerciseEntry.setmActivityType(intent.getIntExtra(StartFragment.ACTIVITY_TYPE, 0));
        mExerciseEntry.setmInputType(intent.getIntExtra(StartFragment.INPUT_TYPE, 0));
        //get time passed in seconds
        if (isNew) {
            mElapsedTime = 0;
            latLngs = new ArrayList<>();
            mExerciseEntry.setmLatLngs(latLngs);
            mExerciseEntry.setmDistance(0);
            mExerciseEntry.setmDuration(0);
            mExerciseEntry.setmCalorie(0);
            mExerciseEntry.setmClimb(0);
        }
        //create timer
        mDurationTimer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                mElapsedTime++;
            }
        };
        mDurationTimer.schedule(timerTask, 0, 1000);
        //date and heart rate should be reset regardless
        mExerciseEntry.setmDateTime(Calendar.getInstance().getTimeInMillis());
        mExerciseEntry.setmHeartRate(0);
    }

    /**
     * Handle location updates
     */
    public void startLocationUpdates(Location location, boolean isFirst) {
        if (location != null && mExerciseEntry != null) {
            synchronized (mExerciseEntry.getmLatLngs()) {
                //add the current location to this entry
                mExerciseEntry.addLatLng(new LatLng(location.getLatitude(), location.getLongitude()));

                //intent to send to MapDisplay
                Intent intent = new Intent();
                intent.setAction(MapDisplayActivity.ACTION);
                if (isFirst)
                    intent.putExtra(MapDisplayActivity.BROADCAST_KEY, MapDisplayActivity.FIRST_LOC);
                else {
                    onUpdate(location);
                    intent.putExtra(MapDisplayActivity.BROADCAST_KEY, MapDisplayActivity.UPDATE_LOC);
                }

                //put current speed to intent
                //speed in m/s -> convert to mi/hr
                intent.putExtra(MapDisplayActivity.CURR_SPEED, location.getSpeed() * (float) 2.2369);

                //send broadcast
                sendBroadcast(intent);
            }
        }
    }

    /**
     * Update the activity type from the weka classifier
     */
    public void startActivityUpdate(double classifyScore) {
        //intent to notify map activity
        Intent intent = new Intent();
        intent.setAction(MapDisplayActivity.ACTION);

        //signal that this the attached data is to update activity type
        intent.putExtra(MapDisplayActivity.BROADCAST_KEY,
                MapDisplayActivity.UPDATE_ACTIVITY);

        //get activity from parameter
        int activityType;
        int intScore = (int) classifyScore;
        if (intScore == 0) {
            //standing
            activityType = 2;
        } else if (intScore == 1) {
            //walking
            activityType = 1;
        } else if (intScore == 2) {
            //running
            activityType = 0;
        } else {
            //other
            activityType = 13;
        }

        intent.putExtra(StartFragment.ACTIVITY_TYPE,
                activityType);

        //broadcast updated activity type to map activity
        sendBroadcast(intent);
        Log.d("Testing", "Sent activity type broadcast. Activity type: " + activityType);
    }

    //*******UPDATE PROCESSES*******//

    /**
     * Update the "current location" and the current stats
     */
    public void onUpdate(Location location) {
        if (location != null) {
            //get the changes in dist and climb
            float deltaDistance = 0;
            float deltaClimb = 0;
            if (prevLocation != null) {
                //update deltas (given in meters)
                deltaDistance = (float) (location.distanceTo(prevLocation) * 0.000621);
                deltaClimb = (float) ((float) (location.getAltitude() - prevLocation.getAltitude()) * 0.000621);
            }
            //set the current location to our new previous
            prevLocation = location;
            //update params
            mExerciseEntry.setmDistance(mExerciseEntry.getmDistance() + deltaDistance);
            mExerciseEntry.setmClimb(mExerciseEntry.getmClimb() + deltaClimb);
            //update distance and calories
            if (mExerciseEntry.getmDistance() > 0) {
                //distance in mi, elapse time in secs.
                mExerciseEntry.setmAvgSpeed((60 * 60 * mExerciseEntry.getmDistance()) / (mElapsedTime));
                mExerciseEntry.setmCalorie((int) (mExerciseEntry.getmDistance() * 100));
            }
        }
    }

    //*****SENSOR CALLBACKS*****//

    /**
     * Store the accelerometer data
     * <p>
     * (adapted from myrunsdatacollecter from here:
     * https://www.cs.dartmouth.edu/~xingdong/Teaching/CS65/code/myrunsdatacollector.zip
     * thank you to original author)
     */
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {

            double m = Math.sqrt(event.values[0] * event.values[0]
                    + event.values[1] * event.values[1] + event.values[2]
                    * event.values[2]);

            // Inserts the specified element into this queue if it is possible
            // to do so immediately without violating capacity restrictions,
            // returning true upon success and throwing an IllegalStateException
            // if no space is currently available. When using a
            // capacity-restricted queue, it is generally preferable to use
            // offer.

            try {
                mAccBuffer.add(m);
            } catch (IllegalStateException e) {

                // Exception happens when reach the capacity.
                // Doubling the buffer. ListBlockingQueue has no such issue,
                // But generally has worse performance
                ArrayBlockingQueue<Double> newBuf = new ArrayBlockingQueue<>(
                        mAccBuffer.size() * 2);

                mAccBuffer.drainTo(newBuf);
                mAccBuffer = newBuf;
                mAccBuffer.add(m);
            }
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    //*****ACTIVITY THREAD*****//

    /**
     * Worker thread that runs continuously to interpret the current activity
     */
    public class ActivityThread extends Thread {
        public void run() {
            //feature vector
            Double[] featVec = new Double[BLOCK_SIZE + 1];

            //fft setup
            int blockSize = 0;
            FFT fft = new FFT(BLOCK_SIZE);

            //acceleration block and fft components
            double[] accBlock = new double[BLOCK_SIZE];
            double[] re = accBlock;
            double[] im = new double[BLOCK_SIZE];
            //run continuously
            while (true) {
                try {
                    //if cancelled, break the loop
                    if (isCancelled) {
                        Log.d("LPC", "activity thread stopped");
                        return;
                    }
                    //dump buffer
                    accBlock[blockSize++] = mAccBuffer.take();

                    if (blockSize == BLOCK_SIZE) {
                        blockSize = 0;

                        //getting max value from block
                        double max = .0;
                        for (double val : accBlock) {
                            if (max < val) {
                                max = val;
                            }
                        }
                        featVec[BLOCK_SIZE] = max;

                        fft.fft(re, im);

                        for (int i = 0; i < re.length; i++) {
                            double mag = Math.sqrt(re[i] * re[i] + im[i]
                                    * im[i]);
                            featVec[i] = mag;
                            im[i] = .0;
                        }

                        //classify the feature vector using the weka classifier
                        double classifyScore = WekaClassifier.classify(featVec);
                        startActivityUpdate(classifyScore);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //*****BREAKDOWN PROCESSES******//

    /**
     * Create a receiver that gets broadcast when map activity tells it to stop the notification
     */
    public class ServiceNotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int req = intent.getIntExtra(STOP_BROADCAST_KEY, 0);
            if (req == STOP_BROADCAST) {
                ((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
                        .cancelAll();
            }
        }
    }

    public void onDestroy() {
        Log.d("LPC", "onDestroy: called");
        //stop location manager & unregister broadcast receiver
        if (locationManager != null) locationManager.removeUpdates(this);
        try {
            unregisterReceiver(serviceNotificationReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        //erase the exercise entry data
        if (mExerciseEntry != null) mExerciseEntry = null;
        //cancel the update thread
        isCancelled = true;
        super.onDestroy();
    }


    //****GETTER METHODS & BINDING****//

    /**
     * Accessible binder
     */
    public class LocationBinder extends Binder {
        public TrackingService getReference() {
            return TrackingService.this;
        }
    }

    public IBinder onBind(Intent intent) {
        return locationBinder;
    }

    public ExerciseEntry getmExerciseEntry() {
        return mExerciseEntry;
    }

    public void setmExerciseEntry(ExerciseEntry exerciseEntry) {
        mExerciseEntry = exerciseEntry;
    }

    public int getmElapsedTime() {
        return mElapsedTime;
    }


    //******LOCATION LISTENER******//
    public void onLocationChanged(Location location) {
        startLocationUpdates(location, false);
    }

    public void onProviderEnabled(String provider) {
    }

    public void onProviderDisabled(String provider) {
    }
}
