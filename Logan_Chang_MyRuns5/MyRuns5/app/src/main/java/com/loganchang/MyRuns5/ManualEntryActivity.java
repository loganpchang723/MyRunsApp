package com.loganchang.MyRuns5;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;
import java.util.HashMap;

public class ManualEntryActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, TimePickerDialog.OnTimeSetListener,
        DatePickerDialog.OnDateSetListener {

    //labels for ListView
    public static final String[] MANUAL_OPTIONS = new String[]{"Date", "Time", "Duration", "Distance",
            "Calories", "Heart Rate", "Comment"};
    //positions in the ListView
    private static final int DATE_POS = 0;
    private static final int TIME_POS = 1;
    private static final int DURATION_POS = 2;
    private static final int DISTANCE_POS = 3;
    private static final int CALORIES_POS = 4;
    private static final int HEARTRATE_POS = 5;
    private static final int COMMNET_POS = 6;

    //tags to restore date and time selections
    private final String DATE_TIME_TAG = "date time tag";
    private static final String TIME_PICKER_TAG = "time picker";
    private static final String HOUR_TAG = "hour";
    private static final String MINUTE_TAG = "minute";
    private static final String DATE_PICKER_TAG = "date picker";
    private static final String YEAR_TAG = "year";
    private static final String MONTH_TAG = "month";
    private static final String DAY_TAG = "day";

    //hashmap value tags
    private static final String MAP_TAG = "map tag";
    private static final String MAP_DATE_TIME_TAG = "map date time tag";
    private static final String MAP_DURATION_TAG = "map duration tag";
    private static final String MAP_DISTANCE_TAG = "map distance tag";
    private static final String MAP_CALORIES_TAG = "map calories tag";
    private static final String MAP_HEARTRATE_TAG = "map heartrate tag";
    private static final String MAP_COMMENT_TAG = "map comment tag";

//    public SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss aa MMM dd yyyy");

    //widgets
    public Calendar mDateAndTime;
    public ListView mListView;
    private MyTimePicker mTimePicker;
    private MyDatePicker mDatePicker;
    private DialogFragment mDialogFragment;

    //flags for visibility of date and time picker dialogs
    private boolean mTimePickerVisible;
    private boolean mDatePickerVisible;

    //database
    public static ExerciseEntry entry;
    private ExerciseEntryDbHelper mDBHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_entry);
        mDateAndTime = Calendar.getInstance();
        //create and bind adapter
        mListView = (ListView) findViewById(R.id.list_view);
        ArrayAdapter<String> manualAdapter = new ArrayAdapter<>(this, R.layout.manual_list_items, MANUAL_OPTIONS);
        mListView.setAdapter(manualAdapter);
        //bind the onClickListener
        mListView.setOnItemClickListener(this);
        //initialize an entry and the db helper
        entry = new ExerciseEntry();

        Bundle bundle = getIntent().getExtras();
        entry.setmInputType(bundle.getInt(StartFragment.INPUT_TYPE,0));
        entry.setmActivityType(bundle.getInt(StartFragment.ACTIVITY_TYPE,0));

        if (savedInstanceState != null) {
            mDateAndTime.setTimeInMillis(savedInstanceState.getLong(DATE_TIME_TAG));
            //if the TimePickerDialog last state was open, restore its state
            if (savedInstanceState.getBoolean(TIME_PICKER_TAG)) {
                mDateAndTime.set(Calendar.HOUR_OF_DAY, savedInstanceState.getInt(HOUR_TAG));
                mDateAndTime.set(Calendar.MINUTE, savedInstanceState.getInt(MINUTE_TAG));
                onTimeClicked();
            }
            //if the DatePickerDialog last state was open, restore its state
            if (savedInstanceState.getBoolean(DATE_PICKER_TAG)) {
                mDateAndTime.set(Calendar.YEAR, savedInstanceState.getInt(YEAR_TAG));
                mDateAndTime.set(Calendar.MONTH, savedInstanceState.getInt(MONTH_TAG));
                mDateAndTime.set(Calendar.DAY_OF_MONTH, savedInstanceState.getInt(DAY_TAG));
                onDateClicked();
            }
            //reload the exercise entry data
            HashMap entryMap = (HashMap) savedInstanceState.getSerializable(MAP_TAG);
//            Log.d("entryMAP", "date time: "+sdf.format(Long.parseLong((String)entryMap.get(MAP_DATE_TIME_TAG))));
//            Log.d("from raw", "date time: "+sdf.format(mDateAndTime.getTimeInMillis()));
            entry.setmDateTime(Long.parseLong((String) entryMap.get(MAP_DATE_TIME_TAG)));
            entry.setmDuration(Integer.parseInt((String) entryMap.get(MAP_DURATION_TAG)));
            entry.setmDistance(Float.parseFloat((String) entryMap.get(MAP_DISTANCE_TAG)));
            entry.setmCalorie(Integer.parseInt((String) entryMap.get(MAP_CALORIES_TAG)));
            entry.setmHeartRate(Integer.parseInt((String) entryMap.get(MAP_HEARTRATE_TAG)));
            entry.setmComment((String)entryMap.get(MAP_COMMENT_TAG));

        }
    }


    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(TIME_PICKER_TAG, mTimePickerVisible);
        outState.putBoolean(DATE_PICKER_TAG, mDatePickerVisible);
//        Log.d("save state", "date time: "+sdf.format(mDateAndTime.getTimeInMillis()));
        outState.putLong(DATE_TIME_TAG, mDateAndTime.getTimeInMillis());
        if (mDateAndTime != null) {
            outState.putInt(HOUR_TAG, mDateAndTime.get(Calendar.HOUR_OF_DAY));
            outState.putInt(MINUTE_TAG, mDateAndTime.get(Calendar.MINUTE));
            outState.putInt(YEAR_TAG, mDateAndTime.get(Calendar.YEAR));
            outState.putInt(MONTH_TAG, mDateAndTime.get(Calendar.MONTH));
            outState.putInt(DAY_TAG, mDateAndTime.get(Calendar.DAY_OF_MONTH));
        }
        //save the entry
        if(entry != null) {
            HashMap<String, String> entryMap = new HashMap<>();
            entryMap.put(MAP_DATE_TIME_TAG, String.valueOf(entry.getmDateTime()));
//            Log.d("entryMAP", "putting in this date time: "+sdf.format(Long.parseLong((String)entryMap.get(MAP_DATE_TIME_TAG))));
            entryMap.put(MAP_DURATION_TAG, String.valueOf(entry.getmDuration()));
            entryMap.put(MAP_DISTANCE_TAG, String.valueOf(entry.getmDistance()));
            entryMap.put(MAP_CALORIES_TAG, String.valueOf(entry.getmCalorie()));
            entryMap.put(MAP_HEARTRATE_TAG, String.valueOf(entry.getmHeartRate()));
            entryMap.put(MAP_COMMENT_TAG, entry.getmComment());
            outState.putSerializable(MAP_TAG, entryMap);
        }
    }

    /**
     * Instantiate DB Helper, if null
     */
    @Override
    public void onResume() {
        super.onResume();
        if(mDBHelper == null) mDBHelper = new ExerciseEntryDbHelper(this);
    }

    /**
     * Close DB Helper on pause
     */
    @Override
    public void onPause() {
        mDBHelper.close();
        super.onPause();
    }

//*********INTERFACE FUNCTIONS*********//

    /**
     * onClickListener callback to create the corresponding dialog fragment
     *
     * @param parent   Parent View
     * @param view     Current View
     * @param position Which item was clicked
     * @param id       ID
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position == DATE_POS) {
            onDateClicked();
        } else if (position == TIME_POS) {
            onTimeClicked();
        } else if (position == DURATION_POS) {
            mDialogFragment =
                    MyRunsDialogFragment.newInstance(MyRunsDialogFragment.DURATION_PICKER_ID);
            mDialogFragment.show(getSupportFragmentManager(), getString(R.string.duration_dialog_title));
        } else if (position == DISTANCE_POS) {
            mDialogFragment =
                    MyRunsDialogFragment.newInstance(MyRunsDialogFragment.DISTANCE_PICKER_ID);
            mDialogFragment.show(getSupportFragmentManager(), getString(R.string.distance_dialog_title));
        } else if (position == CALORIES_POS) {
            mDialogFragment =
                    MyRunsDialogFragment.newInstance(MyRunsDialogFragment.CALORIES_PICKER_ID);
            mDialogFragment.show(getSupportFragmentManager(), getString(R.string.calories_dialog_title));
        } else if (position == HEARTRATE_POS) {
            mDialogFragment =
                    MyRunsDialogFragment.newInstance(MyRunsDialogFragment.HEARTRATE_PICKER_ID);
            mDialogFragment.show(getSupportFragmentManager(), getString(R.string.heartrate_dialog_title));
        } else if (position == COMMNET_POS) {
            mDialogFragment =
                    MyRunsDialogFragment.newInstance(MyRunsDialogFragment.COMMENT_PICKER_ID);
            mDialogFragment.show(getSupportFragmentManager(), getString(R.string.comment_dialog_title));
        }
    }

    /**
     * If "Time" was clicked, display a custom TimePickerDialog
     */
    public void onTimeClicked() {
        //set flag of time picker dialog's visibility to true
        mTimePickerVisible = true;
        mTimePicker = new MyTimePicker(this, this,
                mDateAndTime.get(Calendar.HOUR_OF_DAY),
                mDateAndTime.get(Calendar.MINUTE), false);
        //"cancel" click callback to reset clock
        mTimePicker.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Log.d("onCancel", "cancel callback");
                Calendar now = Calendar.getInstance();
                mTimePicker.updateTime(now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE));
            }
        });
        mTimePicker.show();
    }

    /**
     * TimePicker Listener to save the set time
     *
     * @param view      TimePicker
     * @param hourOfDay Hour chosen
     * @param minute    Minute chosen
     */
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        mDateAndTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
        mDateAndTime.set(Calendar.MINUTE, minute);
        entry.setmDateTime(mDateAndTime.getTimeInMillis());
//        Log.d("onTimeSet", "date time: "+sdf.format(entry.getmDateTime()));
    }

    /**
     * If "Date" was clicked, display a custom DatePickerDialog
     */
    public void onDateClicked() {
        mDatePickerVisible = true;
        mDatePicker = new MyDatePicker(this, this,
                mDateAndTime.get(Calendar.YEAR), mDateAndTime.get(Calendar.MONTH),
                mDateAndTime.get(Calendar.DAY_OF_MONTH));
        //"cancel" click callback to reset calendar
        mDatePicker.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
//                Log.d("onCancel", "cancel callback");
                Calendar now = Calendar.getInstance();
                mDatePicker.updateDate(now.get(Calendar.YEAR), now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH));
            }
        });
        mDatePicker.show();
    }

    /**
     * DatePicker Listener to save the set date
     *
     * @param view       TimePicker
     * @param year       Year chosen
     * @param month      Month chosen
     * @param dayOfMonth Day chosen
     */
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Log.d("onDateSet", "month: "+month);
        mDateAndTime.set(Calendar.YEAR, year);
        mDateAndTime.set(Calendar.MONTH, month);
        mDateAndTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        entry.setmDateTime(mDateAndTime.getTimeInMillis());
//        Log.d("onDateSet", "date time: "+sdf.format(entry.getmDateTime()));
    }

//*********BOTTOM BUTTON HANDLERS*********//

    /**
     * "Cancel" button click callback
     *
     * @param view View
     */
    public void onCancelClick(View view) {
        Toast.makeText(this, getString(R.string.manual_cancel_msg),
                Toast.LENGTH_SHORT).show();
        this.finish();
    }

    /**
     * "Save" button click callback
     *
     * @param view View
     */
    public void onSaveClick(View view) {
        DBWriterThread dbWriterThread = new DBWriterThread(entry);
        dbWriterThread.start();
        this.finish();
    }


//********CUSTOM DATE/TIME PICKER DIALOGS********//

    /**
     * A custom TimePickerDialog that can handle retaining the dialog's instance state on interruption
     */
    private class MyTimePicker extends TimePickerDialog {

        /**
         * Construct TimePickerDialog (same as super class)
         * @param context       Context
         * @param listener      OnTimeSetListener
         * @param hourOfDay     Hour
         * @param minute        Minute
         * @param is24HourView  false
         */
        public MyTimePicker(Context context, OnTimeSetListener listener, int hourOfDay,
                            int minute, boolean is24HourView) {
            super(context, listener, hourOfDay, minute, is24HourView);
        }

        /**
         * Retain the edited time for rotations and other interruptions
         *
         * @param view      TimePicker
         * @param hourOfDay Hour set on the Dialog
         * @param minute    Minute set on the Dialog
         */
        public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
            mDateAndTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
            mDateAndTime.set(Calendar.MINUTE, minute);
        }

        @Override
        public void dismiss() {
            //switch time picker visibility flag to false when dialog dismissed
            mTimePickerVisible = false;
            super.dismiss();
        }
    }

    /**
     * A custom DatePickerDialog that can handle retaining the dialog's instance state on interruption
     */
    private class MyDatePicker extends DatePickerDialog {
        /**
         * Construct DatePickerDialog (same as super class)
         * @param context       Context
         * @param listener      onDateSetListener
         * @param year          Year
         * @param month         Month
         * @param dayOfMonth    Day
         */
        public MyDatePicker(Context context, OnDateSetListener listener, int year, int month, int dayOfMonth) {
            super(context, listener, year, month, dayOfMonth);
        }

        /**
         * Retain the edited month for rotations and other interruptions
         *
         * @param view       TimePicker
         * @param year       Year set on the Dialog
         * @param month      Month set on the Dialog
         * @param dayOfMonth Day set on the Dialog
         */
        public void onDateChanged(DatePicker view, int year, int month, int dayOfMonth) {
            mDateAndTime.set(Calendar.YEAR, year);
            mDateAndTime.set(Calendar.MONTH, month);
            mDateAndTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        }

        @Override
        public void dismiss() {
            //switch date picker visibility flag to false when dialog dismissed
            mDatePickerVisible = false;
            super.dismiss();
        }

    }
    //*****THREAD TO SAVE ENTRY TO DB*****
    private class DBWriterThread extends Thread{
        //local instance vars
        ExerciseEntry eEntry;
        long id;

        /**
         * Construct a thread to writer to db in background
         * @param eEntry    ExerciseEntry being inserted
         */
        public DBWriterThread(ExerciseEntry eEntry){
            this.eEntry = eEntry;
        }

        /**
         * Tell the history fragment's adapter to update the adapter
         * Make toast saying the entry of given id was saved
         */
        private final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if(HistoryFragment.adapter != null) HistoryFragment.adapter.notifyDataSetChanged();
                Toast.makeText(getApplicationContext(), "Entry #"+id+" saved.",
                        Toast.LENGTH_SHORT).show();
            }
        };

        Handler handler = new Handler(Looper.getMainLooper());

        /**
         * Insert a new ExerciseEntry and get its db ID
         */
        public void run(){
            if(mDBHelper == null) mDBHelper = new ExerciseEntryDbHelper(getApplicationContext());
            id = mDBHelper.insertEntry(eEntry);
            handler.post(runnable);
//            Log.d("writing to db", "entry was added with id "+id);
        }
    }
}