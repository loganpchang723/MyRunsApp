package com.loganchang.MyRuns5;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.ListFragment;
import androidx.preference.PreferenceManager;


import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HistoryFragment extends ListFragment {
    public static ActivityEntriesAdapter adapter;
    public static String ENTRY_ID = "entry id";
    public static String FROM_HISTORY = "from history";
    private ExerciseEntryDbHelper mDBHelper;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        Log.d("lifecycle", "onCreate: called");

        //instantiate db helper and bind a new, empty adapter upon creation
        if (getActivity() != null) {

            mDBHelper = new ExerciseEntryDbHelper(getContext());
            adapter = new ActivityEntriesAdapter(getContext(), R.layout.history_list_items, new ArrayList<>());
            Log.d("adapter created", "adapter created");

            setListAdapter(adapter);

        }
    }

    /**
     * Create view
     *
     * @param inflater           LayoutInflater
     * @param container          ViewGroup
     * @param savedInstanceState saved instance state
     * @return view of adapter (list object)
     */
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("lifecycle", "onCreateView: called");
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    /**
     * If this fragment is visible, load in all db entries
     *
     * @param isVisibleToUser is visible?
     */
    public void setMenuVisibility(boolean isVisibleToUser) {
        super.setMenuVisibility(isVisibleToUser);
        if (isVisibleToUser) {
            Log.d("TAG", "setMenuVisibility: is visible");
            LoaderThread loaderThread = new LoaderThread();
            loaderThread.start();
//            setListAdapter(adapter);
        } else Log.d("TAG", "setMenuVisibility: not visible");
    }

    /**
     * Instantiate db helper if null
     */
    public void onResume() {
        super.onResume();
        if (mDBHelper == null) mDBHelper = new ExerciseEntryDbHelper(getContext());
        if (adapter == null )
            adapter = new ActivityEntriesAdapter(getContext(), R.layout.history_list_items, new ArrayList<>());
    }

    /**
     * Close db helper
     */
    @Override
    public void onPause() {
        super.onPause();
        mDBHelper.close();
    }

    /**
     * Open display entry activity for selected list item
     *
     * @param listView ListView
     * @param view     View
     * @param position Position in list
     * @param id       id of selection
     */
    @Override
    public void onListItemClick(@NonNull ListView listView, @NonNull View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);
        //get chosen list item's DB ID
        long entryID = (Long) view.getTag();
        Log.d("id", "id from tag: " + view.getTag());
        ExerciseEntry eEntry = mDBHelper.fetchEntryByIndex(entryID);
        //intent to go to new activity
        Intent intent = null;
        if (eEntry.getmInputType() == 0) {
            intent = new Intent(getActivity(), DisplayEntryActivity.class);
        } else intent = new Intent(getActivity(), MapDisplayActivity.class);
        intent.putExtra(ENTRY_ID, entryID);
        intent.putExtra(FROM_HISTORY,true);
        getActivity().startActivity(intent);
    }

//*****ADAPTER FOR THE LIST VIEW*****//
    public class ActivityEntriesAdapter extends ArrayAdapter<ExerciseEntry> {
        private final Context mContext;
        private final List<ExerciseEntry> entries;

        /**
         * Constructor for custom adapter
         *
         * @param mContext   Context
         * @param resourceID Layout file
         * @param entries    List of ExerciseEntries to pass to ListView
         */
        public ActivityEntriesAdapter(Context mContext, int resourceID, List<ExerciseEntry> entries) {
            super(mContext, resourceID, entries);
            this.mContext = mContext;
            this.entries = entries;
        }

        /**
         * Format the two lines of each entry in the list view
         *
         * @param position    position in list
         * @param convertView view to show
         * @param parent      parent view group
         * @return view of final list object
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.history_list_items, parent, false);
            }

            ExerciseEntry eEntry = entries.get(position);

            //set the first line of the view
            TextView firstLine = (TextView) convertView.findViewById(R.id.history_list_first_line);
            firstLine.setText(formatFirstLine(eEntry));

            //set the second line of the view
            TextView secondLine = (TextView) convertView.findViewById(R.id.history_list_second_line);
            secondLine.setText(formatSecondLine(eEntry));

            //set the id of each view
            convertView.setTag(eEntry.getId());

            return convertView;
        }

        /**
         * Re-fetch all entries when the db is changed so the list view is updated
         */
        @Override
        public void notifyDataSetChanged() {
            Log.d("notifyDataSetChanged", "notifyDataSetChanged: called");
            Log.d("notifyDataSetChanged", "is db helper null: " + (mDBHelper == null));
            if (mDBHelper != null) {
                //get all the entries now in the db on worker thread
                LoaderThread loaderThread = new LoaderThread();
                loaderThread.start();
            }
            super.notifyDataSetChanged();
        }

        /**
         * Returns item at position
         *
         * @param position position in list
         * @return item at position
         */
        @Override
        public ExerciseEntry getItem(int position) {
            return entries.get(position);
        }

        /**
         * Get item id from position in list
         *
         * @param position position in list
         * @return item's id
         */
        @Override
        public long getItemId(int position) {
            return position;
        }

        /**
         * Get number of items in adapter
         *
         * @return number of items in adapter
         */
        @Override
        public int getCount() {
            return entries.size();
        }



//****FORMATTING DISPLAY FUNCTIONS****//

        /**
         * Gets and formats the first line of the history entry
         */
        private String formatFirstLine(ExerciseEntry entry) {

            Log.d("input type", "input type: " + entry.getmInputType());

            String input = StartFragment.INPUT_TO_ID[entry.getmInputType()];
            String activity = StartFragment.ACTIVITY_TO_ID[entry.getmActivityType()];
            String dateTime = formatDateTime(entry.getmDateTime());
            return input + ": " + activity + ", " + dateTime;
        }

        /**
         * Gets and formats the second line of the history entry
         */
        private String formatSecondLine(ExerciseEntry entry) {

            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String unitPref = pref.getString(getString(R.string.unit_preference), getString(R.string.unit_miles));
            String distance = formatDistance(entry.getmDistance(), unitPref);
            String duration = formatDuration(entry.getmDuration());
            return distance + ", " + duration;
        }
    }

//*******FORMATTING HELPER FUNCTIONS*******//

    /**
     * Convert the date and time from milliseconds to readable format
     *
     * @param dateTime dateTime in millis
     * @return date in hour:minute:second AM/PM month day year
     */
    public static String formatDateTime(long dateTime) {
        Date date = new Date(dateTime);
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss aa MMM dd yyyy");
        return sdf.format(date);
    }

    /**
     * Convert the duration from seconds to the proper format
     *
     * @param duration duration
     * @return duration in terms of mintues and seconds
     */
    public static String formatDuration(double duration) {
        int minutes = (int) (duration / 60);
        int seconds = (int) (duration % 60);
        if (minutes == 0 && seconds == 0) return "0 secs";
        return "" + minutes + " min " + seconds + " secs";
    }

    /**
     * Convert the distance from kilometers to the proper format
     *
     * @param distance distance value
     * @param unitPref km or mi
     * @return distance in the specifiec unit
     */
    public static String formatDistance(double distance, String unitPref) {

        if (unitPref.equals("Kilometers")) {
            distance /= 0.621371; // converts from km to miles
        }
        return String.format("%.2f", distance) + " " + unitPref;
    }

//*******THREAD FOR LOADING IN ALL THE ENTRIES*******//
    private class LoaderThread extends Thread {
        List<ExerciseEntry> list = null;

        /**
         * Create a new adapter with the updated list of db entries
         * Bind this new adapter
         */
        private final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                adapter = new ActivityEntriesAdapter(getContext(), R.layout.history_list_items, list);
                setListAdapter(adapter);
            }
        };

        Handler handler = new Handler(Looper.getMainLooper());

        /**
         * Fetch all the entries from db
         */
        public void run() {
//            if (mDBHelper == null) mDBHelper = new ExerciseEntryDbHelper();
            if (mDBHelper != null) list = mDBHelper.fetchAllEntries();
            Log.d("thread run", "is list null " + (list == null));
            handler.post(runnable);
            Log.d("loader thread done", "loader thread done ");
        }
    }
}