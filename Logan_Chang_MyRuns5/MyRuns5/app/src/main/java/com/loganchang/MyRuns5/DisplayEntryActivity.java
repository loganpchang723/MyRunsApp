package com.loganchang.MyRuns5;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

public class DisplayEntryActivity extends AppCompatActivity {
    //instance vars
    private long mEntryID;
    private ExerciseEntryDbHelper mDBHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_entry);

        //instantiate the db helper
        mDBHelper = new ExerciseEntryDbHelper(this);

        //get intent info and chosen exercise entry by its db id
        Bundle bundle = getIntent().getExtras();
        mEntryID = bundle.getLong(HistoryFragment.ENTRY_ID);
        ExerciseEntry eEntry = mDBHelper.fetchEntryByIndex(mEntryID);

        //set up and fill in widgets (edit texts)

        //input type
        EditText inputText = (EditText) findViewById(R.id.display_input);
        inputText.setText(StartFragment.INPUT_TO_ID[eEntry.getmInputType()]);
        //activity type
        EditText activityText = (EditText) findViewById(R.id.activity_input);
        activityText.setText(StartFragment.ACTIVITY_TO_ID[eEntry.getmActivityType()]);
        //date time
        EditText dateTimeText = (EditText) findViewById(R.id.date_time_input);
        dateTimeText.setText(HistoryFragment.formatDateTime(eEntry.getmDateTime()));
        //duration
        EditText durationText = (EditText) findViewById(R.id.duration_input);
        durationText.setText(HistoryFragment.formatDuration(eEntry.getmDuration()));
        //distance
        EditText distanceText = (EditText) findViewById(R.id.distance_input);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String unitPref = pref.getString(getString(R.string.unit_preference), getString(R.string.unit_miles));
        distanceText.setText(HistoryFragment.formatDistance(eEntry.getmDistance(), unitPref));
        //calories
        EditText caloriesText = (EditText) findViewById(R.id.calorie_input);
        caloriesText.setText(eEntry.getmCalorie()+" cals");
        //heart rate
        EditText heartrateText = (EditText) findViewById(R.id.heartrate_input);
        heartrateText.setText(eEntry.getmHeartRate()+" bpm");
    }

    /**
     * create "DELETE" button in top menu
     * @param menu  Menu where DELETE button resides
     * @return      true
     */
    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        menu.add(Menu.NONE,0,0,"DELETE").
                setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    /**
     * remove item from database when DELETE is pressed
     * @param item  the DELETE button
     * @return      true
     */
    public boolean onOptionsItemSelected(MenuItem item){
        super.onOptionsItemSelected(item);
        if(HistoryFragment.adapter.getCount()>0) {
            DeleteThread deleteThread = new DeleteThread();
            deleteThread.start();
        }
        this.finish();
        return true;
    }

    public void onResume(){
        super.onResume();
        //rebind db helper
        if(mDBHelper == null) mDBHelper = new ExerciseEntryDbHelper(this);
    }
    public void onPause(){
        //close the db helper
        mDBHelper.close();
        super.onPause();
    }

    /**
     * Worker thread to run deletion on
     */
    private class DeleteThread extends Thread{
        //runnable to update adapter and UI in history fragment and show toast
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                HistoryFragment.adapter.notifyDataSetChanged();
                Toast.makeText(getApplicationContext(), "Entry #"+mEntryID+" deleted.",
                        Toast.LENGTH_SHORT).show();
            }
        };
        Handler handler  = new Handler(Looper.getMainLooper());
        //run the deletion on a worker thread
        public void run(){
            if(mDBHelper != null) mDBHelper.removeEntry(mEntryID);
            handler.post(runnable);
        }
    }
}