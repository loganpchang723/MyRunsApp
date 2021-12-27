package com.loganchang.MyRuns5;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class UserProfileActivity extends AppCompatActivity {
    public static final int REQUEST_CODE_TAKE_FROM_CAMERA = 1;
    public static final int REQUEST_CODE_PICK_FROM_GALLERY = 2;
    public static final String URI_INSTANCE_STATE_KEY = "saved_uri";
    public final String TAG = "LPC";

    //SharedPreferences object, UI widgets, file names + Uris
    private SharedPreferences sharedPref;
    private EditText nameField;
    private EditText emailField;
    private EditText phoneField;
    private EditText classField;
    private EditText majorField;
    private RadioButton maleButton;
    private RadioButton femaleButton;
    private ImageView imageView;
    private String tempRawImgFileName;
    private String tempCropImgFileName;
    private String saveImgFileName;
    private String showImgFileName;
    private Uri tempRawImgUri;
    private Uri tempCropImgUri;
    private Uri saveImgUri;
    private Uri showImgUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set up UI and instantiate instance variables
        setContentView(R.layout.activity_user_profile);
        tempRawImgFileName = getString(R.string.tempRawImgFileName);
        tempCropImgFileName = getString(R.string.tempCropImgFileName);
        saveImgFileName = getString(R.string.saveImgFileName);
        showImgFileName = getString(R.string.showImgFileName);
        nameField = (EditText) findViewById(R.id.name_field);
        emailField = (EditText) findViewById(R.id.email_field);
        phoneField = (EditText) findViewById(R.id.phone_field);
        classField = (EditText) findViewById(R.id.class_field);
        majorField = (EditText) findViewById(R.id.major_field);
        maleButton = (RadioButton) findViewById(R.id.male_button);
        femaleButton = (RadioButton) findViewById(R.id.female_button);
        imageView = (ImageView) findViewById(R.id.imageProfile);
        tempRawImgUri = FileProvider.getUriForFile(this, "com.loganchang.MyRuns5", new File(getExternalFilesDir(null), tempRawImgFileName));
        //check permissions
        checkPermissions();
        //load saved profile
        loadProfile();
        //load the most recent photo that was in the imageView into imageView (savedInstanceState)
        if (savedInstanceState != null) {
            showImgUri = savedInstanceState.getParcelable(URI_INSTANCE_STATE_KEY);
            imageView.setImageURI(null);
            imageView.setImageURI(showImgUri);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //if an image has been taken and cropped in this lifecycle, save its Uri
        if (showImgUri != null) {
            outState.putParcelable(URI_INSTANCE_STATE_KEY, showImgUri);
        }
        //else, save the savedImgUri
        else {
            outState.putParcelable(URI_INSTANCE_STATE_KEY, saveImgUri);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode != RESULT_OK) {
            //delete any temporary files as they are only used during an active photo+crop session
            if (tempRawImgUri != null) deleteTempFile("raw");
            if (tempCropImgUri != null) deleteTempFile("crop");
            return;
        }
        //begin cropping if photo taken or chosen from gallery, else if crop requested, handle the request
        if (requestCode == REQUEST_CODE_TAKE_FROM_CAMERA) {
            beginCrop(tempRawImgUri);
        } else if (requestCode == REQUEST_CODE_PICK_FROM_GALLERY) {
            beginCrop(intent.getData());
        } else if (requestCode == Crop.REQUEST_CROP) {
            //delete the temporary files before handling the crop event, if it exists
            if (tempRawImgUri != null) deleteTempFile("raw");
            handleCrop(resultCode, intent);
            if (tempCropImgUri != null) deleteTempFile("crop");
        }
    }
    /**
     * Prompts user permission for access to camera and files
     */
    public void checkPermissions() {
        if (Build.VERSION.SDK_INT < 23)
            return;
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 0);
        }
    }
//*****PROFILE LOADING METHODS*****//
    /**
     * When the activity is created, load in saved field data + saved image
     */
    public void loadProfile() {
        sharedPref = this.getPreferences(MODE_PRIVATE);
        nameField = ((EditText) findViewById(R.id.name_field));
        nameField.setText(sharedPref.getString(getString(R.string.name_key), ""));
        //input all saved field data
        emailField.setText(sharedPref.getString(getString(R.string.email_key), ""));
        phoneField.setText(sharedPref.getString(getString(R.string.phone_key), ""));
        maleButton.setChecked(sharedPref.getBoolean(getString(R.string.male_key), false));
        femaleButton.setChecked(sharedPref.getBoolean(getString(R.string.female_key), false));
        classField.setText(sharedPref.getString(getString(R.string.class_key), ""));
        majorField.setText(sharedPref.getString(getString(R.string.major_key), ""));
        //load in the saved profile image
        imageView.setImageURI(null);
        loadImg();
    }
    /**
     * Called in onCreate() to load last saved profile picture
     */
    public void loadImg() {
        imageView.setImageURI(null);
        try {
            saveImgUri = FileProvider.getUriForFile(this, "com.loganchang.MyRuns5", new File(getExternalFilesDir(null), saveImgFileName));
            imageView.setImageURI(saveImgUri);
        } catch (NullPointerException e) {
            Log.d(TAG, "loadImg: setting imageView to null");
        }
    }

//*****BUTTON CALLBACKS*****//
    /**
     * Called when the "Change" button is pressed to show the choosing dialog
     *
     * @param view View parameter
     */
    public void onChangePhotoClicked(View view) {
        //create and show a the photo picker dialog
        DialogFragment photoDialog = MyRunsDialogFragment.newInstance(MyRunsDialogFragment.PHOTO_PICKER_ID);
        photoDialog.show(getSupportFragmentManager(), getString(R.string.photo_dialog_title));
    }

    /**
     * Start and store the correct Activity depending on if taking from camera or choosing from library
     *
     * @param selectionID Taking from camera or choosing from library
     */
    public void handlePhotoDialog(int selectionID) {
        Intent intent;
        //start intent for taking picture from camera
        if (selectionID == MyRunsDialogFragment.SELECT_FROM_CAMERA) {
            intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, tempRawImgUri);
            startActivityForResult(intent, REQUEST_CODE_TAKE_FROM_CAMERA);
        }
        //start intent for choosing photo from gallery
        else if (selectionID == MyRunsDialogFragment.SELECT_FROM_GALLERY) {
            intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, tempRawImgUri);
            startActivityForResult(intent, REQUEST_CODE_PICK_FROM_GALLERY);
        }
    }

    /**
     * Called when the "save" button is clicked
     *
     * @param view View parameter
     */
    public void saveProfile(View view) {
        //store in shared preferences
        sharedPref = this.getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        //store all the input values in the shared preferences object
        editor.putString(getString(R.string.name_key), nameField.getText().toString());
        editor.putString(getString(R.string.email_key), emailField.getText().toString());
        editor.putString(getString(R.string.phone_key), phoneField.getText().toString());
        editor.putString(getString(R.string.class_key), classField.getText().toString());
        editor.putString(getString(R.string.major_key), majorField.getText().toString());
        editor.putBoolean(getString(R.string.male_key), maleButton.isChecked());
        editor.putBoolean(getString(R.string.female_key), femaleButton.isChecked());
        editor.apply();
        //save image
        saveImg();
        //toast saying saved
        Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show();
        //kill the activity
        this.finish();
    }
    /**
     * Saves the profile image to savedImgFile
     */
    public void saveImg() {
        //write tempImgUri contents to saveImgUri
        saveImgUri = FileProvider.getUriForFile(this, "com.loganchang.MyRuns5", new File(getExternalFilesDir(null), saveImgFileName));
        try {
            transferFileContents(showImgUri, saveImgUri);
        } catch (NullPointerException e) {
            return;
        }
    }

    /**
     * Called when "Cancel" button pressed to kill the activity
     *
     * @param view View parameter
     */
    public void onCancelButton(View view) {
        this.finish();
    }

//******CROP EVENT METHODS******//

    /**
     * Triggers Crop activity of temp image (raw, not cropped) to be cropped and saved in show image file
     *
     * @param source Uri of image to be cropped
     */
    public void beginCrop(Uri source) {
        tempCropImgUri = FileProvider.getUriForFile(this, "com.loganchang.MyRuns5", new File(getExternalFilesDir(null), tempCropImgFileName));
        Crop.of(source, tempCropImgUri).asSquare().start(this);
    }

    /**
     * After the crop event is over, handle it accordingly
     *
     * @param resultCode Measures status of crop
     * @param result     Intent carrying cropped image
     */
    public void handleCrop(int resultCode, Intent result) {
        //if the crop was successful, store it in the showImgFile and show it in imageView
        if (resultCode == RESULT_OK) {
            imageView.setImageURI(null);
            showImgUri = FileProvider.getUriForFile(this, "com.loganchang.MyRuns5", new File(getExternalFilesDir(null), showImgFileName));
            transferFileContents(Crop.getOutput(result), showImgUri);
            imageView.setImageURI(showImgUri);
        }
        //if crop unsuccessful, tell the user
        else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(this, Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

//****UTILITY FUNCTIONS****//
    /**
     * Delete one of the specified tempImgFiles
     *
     * @param name Either "raw" or "crop" to indicate which file to be deleted
     */
    public void deleteTempFile(String name) {
        if (name.equals("raw")) {
            File tempRawImgFile = new File(tempRawImgUri.getPath());
            if (tempRawImgFile.exists()) {
                tempRawImgFile.delete();
            }
        } else if (name.equals("crop")) {
            File tempCropImgFile = new File(tempCropImgUri.getPath());
            if (tempCropImgFile.exists()) {
                tempCropImgFile.delete();
            }
        }
    }

    /**
     * Helper function to read file contents from source file and write these contents to destination file
     *
     * @param sourceUri      Uri of source file
     * @param destinationUri Uri of destination file
     */
    public void transferFileContents(Uri sourceUri, Uri destinationUri) {
        //set up files and streams
        File sourceFile = new File(sourceUri.getPath());
        File destinationFile = new File(destinationUri.getPath());
        FileInputStream in = null;
        FileOutputStream out = null;
        //try reading contents of sourceFile and writing to destinationFile
        try {
            in = new FileInputStream(sourceFile);
            out = new FileOutputStream(destinationFile);
            int length;
            byte[] buffer = new byte[1024];
            while ((length = in.read(buffer)) > 0) out.write(buffer, 0, length);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //close & flush streams
            try {
                if (in != null) in.close();
                if (out != null) {
                    out.flush();
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



}