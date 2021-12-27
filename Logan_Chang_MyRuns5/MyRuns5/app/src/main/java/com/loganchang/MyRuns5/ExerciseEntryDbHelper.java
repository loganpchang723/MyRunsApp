package com.loganchang.MyRuns5;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class ExerciseEntryDbHelper extends SQLiteOpenHelper {
    //db info (file name, version, table name)
    private static final String DATABASE_NAME = "exercise_entries.db";
    private static final int DATABASE_VERSION = 1;
    public static final String TABLE_NAME_ENTRIES = "entry";

    //keys for db columns
    public static final String COL_ROW = "_id";
    public static final String COL_INPUT_TYPE = "input_type";
    public static final String COL_ACTIVITY_TYPE = "activity_type";
    public static final String COL_DATE_TIME = "date_time";
    public static final String COL_DURATION = "duration";
    public static final String COL_DISTANCE = "distance";
    public static final String COL_AVG_PACE = "avg_pace";
    public static final String COL_AVG_SPEED = "avg_speed";
    public static final String COL_CALORIES = "calories";
    public static final String COL_CLIMB = "climb";
    public static final String COL_HEART_RATE = "heartrate";
    public static final String COL_COMMENT = "comment";
    public static final String COL_PRIVACY = "privacy";
    public static final String COL_GPS_DATA = "gps_data";

    private SQLiteDatabase database;

    //db table schema
    public static final String CREATE_TABLE_ENTRIES = "CREATE TABLE IF NOT EXISTS "
            + TABLE_NAME_ENTRIES + " ("
            + COL_ROW + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COL_INPUT_TYPE + " INTEGER NOT NULL, "
            + COL_ACTIVITY_TYPE + " INTEGER NOT NULL, "
            + COL_DATE_TIME + " DATETIME NOT NULL, "
            + COL_DURATION + " FLOAT, "
            + COL_DISTANCE + " FLOAT, "
            + COL_AVG_PACE + " FLOAT, "
            + COL_AVG_SPEED + " FLOAT,"
            + COL_CALORIES + " INTEGER, "
            + COL_CLIMB + " FLOAT, "
            + COL_HEART_RATE + " INTEGER, "
            + COL_COMMENT + " TEXT, "
            + COL_PRIVACY + " INTEGER,"
            + COL_GPS_DATA + " BLOB" +");";

    //Following method headers and comments are from course web page:
    //https://www.cs.dartmouth.edu/~xingdong/Teaching/CS65/myruns/database.html


    public ExerciseEntryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Execute the db creation
     */
    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(CREATE_TABLE_ENTRIES);
    }

    /**
     * Handle table upgrades
     *
     * @param db         database
     * @param oldVersion old version number
     * @param newVersion new version number
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_ENTRIES);
        onCreate(db);
    }

    /**
     * Insert an ExerciseEntry into db
     */
    public long insertEntry(ExerciseEntry entry) {
        //put all the column values into a new row
        ContentValues values = new ContentValues();
        values.put(COL_INPUT_TYPE, entry.getmInputType());
        values.put(COL_ACTIVITY_TYPE, entry.getmActivityType());
        values.put(COL_DATE_TIME, entry.getmDateTime());
        values.put(COL_DURATION, entry.getmDuration());
        values.put(COL_DISTANCE, entry.getmDistance());
        values.put(COL_AVG_PACE, entry.getmAvgPace());
        values.put(COL_AVG_SPEED, entry.getmAvgSpeed());
        values.put(COL_CALORIES, entry.getmCalorie());
        values.put(COL_CLIMB, entry.getmClimb());
        values.put(COL_HEART_RATE, entry.getmHeartRate());
        values.put(COL_COMMENT, entry.getmComment());
        //change latlng list of positions to byte array
        Gson gson = new Gson();
        String json = gson.toJson(entry.getmLatLngs());
        Log.d("LPC", "insertEntry: saving lat lngs list of length "+entry.getmLatLngs().size());
        values.put(COL_GPS_DATA, json.getBytes());

        //insert new row into db
//        SQLiteDatabase database = getWritableDatabase();
        database = getWritableDatabase();
        long insertedId = database.insert(TABLE_NAME_ENTRIES, null, values);
        //close database
        database.close();
        return insertedId;
    }

    /**
     * Remove an entry by its id in the db
     */
    public void removeEntry(long id) {
//        SQLiteDatabase database = getWritableDatabase();
        database = getWritableDatabase();
        database.delete(TABLE_NAME_ENTRIES,
                COL_ROW + " = " + id, null);
        //close database
        database.close();

    }

    /**
     * Query a specific entry by its id.
     * @param rowId     id of the entry to fetch
     * @return          the ExerciseEntry object at rowId
     */
    public ExerciseEntry fetchEntryByIndex(long rowId) {
//        SQLiteDatabase database = getWritableDatabase();
        database = getWritableDatabase();
        Cursor cursor = database.query(TABLE_NAME_ENTRIES, null,
                COL_ROW + " = " + rowId, null, null,
                null, null);
        cursor.moveToFirst();
        ExerciseEntry entry = covertCursorToEntry(cursor);

        //close cursor and database
        cursor.close();
        database.close();

        return entry;
    }

    /**
     * Query the entire table, return all rows
     * @return  all ExerciseEntry objects in the db in a list
     */
    public ArrayList<ExerciseEntry> fetchAllEntries() {
        Log.d("fetch all", "fetchAllEntries: here");
//                SQLiteDatabase database = getWritableDatabase();
        database = getWritableDatabase();
        ArrayList<ExerciseEntry> entries = new ArrayList<>();
        Cursor cursor = database.query(TABLE_NAME_ENTRIES,
                null, null, null, null, null, null);

        cursor.moveToFirst();
        //loop through all entries
        while (!cursor.isAfterLast()) {
            ExerciseEntry comment = covertCursorToEntry(cursor);
            entries.add(comment);
            cursor.moveToNext();
        }
        //close cursor and database
        cursor.close();
        database.close();
        return entries;
    }

    /**
     * Get the ExerciseEntry at the Cursor
     * @param cursor    Cursor
     * @return          ExerciseEntry at the cursor
     */
    public ExerciseEntry covertCursorToEntry(Cursor cursor) {
        //make an entry and fill in its values
        ExerciseEntry entry = new ExerciseEntry();
        entry.setId(cursor.getLong(0));
        entry.setmInputType(cursor.getInt(1));
        entry.setmActivityType(cursor.getInt(2));
        entry.setmDateTime(cursor.getLong(3));
        entry.setmDuration(cursor.getInt(4));
        entry.setmDistance(cursor.getFloat(5));
        entry.setmAvgPace(cursor.getFloat(6));
        entry.setmAvgSpeed(cursor.getFloat(7));
        entry.setmCalorie(cursor.getInt(8));
        entry.setmClimb(cursor.getFloat(9));
        entry.setmHeartRate(cursor.getInt(10));
        entry.setmComment(cursor.getString(11));
        //convert byte array to ArrayList of LatLng
        Gson gson = new Gson();
//        try {
        if(cursor.getBlob(13) != null) {
            String json = new String(cursor.getBlob(13));
            Type type = new TypeToken<ArrayList<LatLng>>() {
            }.getType();
//        entry.setmLatLngs((ArrayList<LatLng>)gson.fromJson(json, type));
            entry.setmLatLngs(gson.fromJson(json, type));
            Log.d("LPC", "retrieved and turned lat lngs into list ");
//        } catch (NullPointerException e){
//            Log.d("LPC", "lat lngs null ");
//            e.printStackTrace();
//        }
        } else {
            Log.d("LPC", "lat lngs null ");
        }

        return entry;
    }
}
