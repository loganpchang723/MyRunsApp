package com.loganchang.MyRuns5;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;

import android.text.InputType;
import android.util.Log;
import android.widget.EditText;


public class MyRunsDialogFragment extends DialogFragment {

    private static final String DIALOG_ID_KEY = "dialog id";

    //dialog ids and titles
    public static final int PHOTO_PICKER_ID = 0;
    public static final int DURATION_PICKER_ID = 1;
    public static final int DISTANCE_PICKER_ID = 2;
    public static final int CALORIES_PICKER_ID = 3;
    public static final int HEARTRATE_PICKER_ID = 4;
    public static final int COMMENT_PICKER_ID = 5;
    public static final int[] ID_LABEL = {R.string.photo_dialog_title, R.string.duration_dialog_title,
            R.string.distance_dialog_title, R.string.calories_dialog_title, R.string.heartrate_dialog_title,
            R.string.comment_dialog_title};


    //selection options for photo picker
    public static final int SELECT_FROM_CAMERA = 0;
    public static final int SELECT_FROM_GALLERY = 1;

    //storing EditText data
    public static final String TEXT_TAG = "text";
    private EditText mEditText;


    /**
     * Create a new instance of DialogFragment
     *
     * @param dialog_id Which category DialogFragment is being created for
     * @return DialogFragment for the specified category
     */
    public static MyRunsDialogFragment newInstance(int dialog_id) {
        MyRunsDialogFragment dialogFrag = new MyRunsDialogFragment();
        Bundle inputBundle = new Bundle();
        inputBundle.putInt(DIALOG_ID_KEY, dialog_id);
        dialogFrag.setArguments(inputBundle);

        return dialogFrag;
    }

    /**
     * Save the text in the editText, if there is any, and put it in the outState Bundle
     *
     * @param outState outState Bundle
     */
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Dialog dialog = getDialog();
        if (dialog != null && mEditText != null) {
            Log.d("MY_TAG", "DialogFragment-onSaveInstanceState: text here = " + mEditText.getText().toString());
            outState.putString(TEXT_TAG, mEditText.getText().toString());
        }
    }

    /**
     * Determine which dialog to create (photo picker or editText)
     *
     * @param savedInstanceState Saved instance state Bundle
     * @return Either a photo dialog to choose source of profile photo or editText dialog
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        int dialog_id = getArguments().getInt(DIALOG_ID_KEY);
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        //save the text that was in the EditText, if there was any, so it can be passed to new dialog
        String prevText = "";
        if (savedInstanceState != null) {
            prevText = savedInstanceState.getString(TEXT_TAG);
        }

        //create either photo picker or edit text dialog
        if (dialog_id == PHOTO_PICKER_ID) {
            buildPhotoDialog(dialogBuilder);
            return dialogBuilder.create();
        } else if (dialog_id >= DURATION_PICKER_ID && dialog_id <= COMMENT_PICKER_ID) {
            buildEditTextDialog(dialogBuilder, prevText, dialog_id);
            return dialogBuilder.create();
        } else {
            return null;
        }
    }

    @Override
    public void onDestroyView() {
        Dialog dialog = getDialog();
        //don't dismiss the dialog box on rotation
        if (dialog != null && getRetainInstance()) {
            dialog.setDismissMessage(null);
        }
        //erase any instance of the editText now that the dialog is dismissed
        mEditText = null;
        super.onDestroyView();
    }


//******DIALOG BUILDING METHODS******//

    /**
     * Builds the profile photo dialog
     *
     * @param dialogBuilder DialogBuilder
     */
    private void buildPhotoDialog(AlertDialog.Builder dialogBuilder) {
        //set up title
        dialogBuilder.setTitle(R.string.photo_dialog_title);

        //create listener
        DialogInterface.OnClickListener photoPickerListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        ((UserProfileActivity) getContext()).handlePhotoDialog(item);
                    }
                };

        //bind listener
        dialogBuilder.setItems(R.array.photo_dialog_options, photoPickerListener);
    }

    /**
     * Build the edit number or text dialog
     *
     * @param dialogBuilder DialogBuilder
     * @param prevText      Text previously in the EditText
     * @param id            Which category was chosen
     */
    private void buildEditTextDialog(final AlertDialog.Builder dialogBuilder, final String prevText, int id) {
        //set up title
        dialogBuilder.setTitle(getString(ID_LABEL[id]));

        //create editText and fill it with prevText
        mEditText = new EditText(getContext());
        mEditText.setText(prevText);

        //set input type to "QWERTY" for "Comment" section, else set input type to a number keypad
        if (id == COMMENT_PICKER_ID) {
            mEditText.setInputType(InputType.TYPE_CLASS_TEXT);
            mEditText.setHint(R.string.comment_dialog_hint);
        } else {
            mEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        }

        //put the editText into the dialog's view
        dialogBuilder.setView(mEditText);

        //callback for clicking "OK"
        dialogBuilder.setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int buttonId) {
                String input = mEditText.getText().toString();

                //set the current entry's attributes when "OK" is clicked
                if (id == DURATION_PICKER_ID) {
                    int timeSecs = 0;
                    if (!input.equals("")) timeSecs = Integer.parseInt(input) * 60;
                    ManualEntryActivity.entry.setmDuration(timeSecs);
                } else if (id == DISTANCE_PICKER_ID) {
                    float dist = 0;
                    if (!input.equals("")) dist = Float.parseFloat(input);
                    //get the distance unit (km or mi)
                    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    String prefUnit = pref.getString(getString(R.string.unit_preference),
                            getString(R.string.unit_miles));
                    //convert to miles if in km
                    if (prefUnit.equals("Kilometers")) dist *= 0.621371;
                    ManualEntryActivity.entry.setmDistance(dist);
                } else if (id == CALORIES_PICKER_ID) {
                    int cals = 0;
                    if (!input.equals("")) cals = Integer.parseInt(input);
                    ManualEntryActivity.entry.setmCalorie(cals);
                } else if (id == HEARTRATE_PICKER_ID) {
                    int hr = 0;
                    if (!input.equals("")) hr = Integer.parseInt(input);
                    ManualEntryActivity.entry.setmHeartRate(hr);
                } else if (id == COMMENT_PICKER_ID) {
                    ManualEntryActivity.entry.setmComment(input);
                }
            }
        });

        //callback for clicking "cancel"
        dialogBuilder.setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int buttonId) {
            }
        });
    }
}
