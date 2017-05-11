package com.kasach.photobysms;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.DatabaseUtils;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.app.Activity;
import java.io.FileDescriptor;
import java.io.IOException;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.database.sqlite.*;

import android.app.AlertDialog.*;
import android.util.Log;

public class dashboard extends AppCompatActivity {
    private static int RESULT_LOAD_IMAGE = 1;
    ProgressDialog mDialog;
    BitmapHandler bitmapHandler;
    DatabaseHandler databaseHandler;
    private static int MAX_DIMENSION = 500;
    private static int COMPRESSION_QUALITY = 70;


    /////////////////////////////////////////////
    // Initialize The Page
    /////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ////////////////////////////
        // Initialize Properties
        ////////////////////////////
        mDialog = new ProgressDialog(dashboard.this);
        bitmapHandler = new BitmapHandler(dashboard.this);
        databaseHandler = new DatabaseHandler(dashboard.this);

        ///////////////////////////
        // Initialize the View
        ///////////////////////////
        setContentView(R.layout.activity_dashboard);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ///////////////////////////
        // Initialize Choose Image Button
        ///////////////////////////
        Button button_select_image = (Button) findViewById(R.id.button_select_image);
        button_select_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });

        ///////////////////////////
        // Initialize Send Image Button
        ///////////////////////////
        Button button_send_image = (Button) findViewById(R.id.button_send_image);
        button_send_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendTheImageToServer();
            }
        });


        // The request code used in ActivityCompat.requestPermissions()
        // and returned in the Activity's onRequestPermissionsResult()
        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {Manifest.permission.READ_SMS, Manifest.permission.SEND_SMS};
        if(!hasPermissions(this, PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }

    }
    ///////////////////////////


    ///////////////////////
    // Check if given permission
    ///////////////////////
    public static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }


    protected void sendTheImageToServer(){
        Log.i("sendImageToServer", "Here i is.");
        displayLoadingDialog();
        ImageView imageView = (ImageView) findViewById(R.id.imgView);
        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        new EncodeTheImage().execute(drawable);
    }

    private void sendSMS(String phoneNumber, String message) {
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, null, null);
    }

    protected void continueSendingToServer(Photo thisPhoto){

        ///////////////
        // `Register` the photo
        ///////////////
        Boolean result = databaseHandler.saveNewPhoto(thisPhoto);
        Log.i("save result", result + "");

        //int count = databaseHandler.returnCountOf("IN_PROGRESS");
        //Log.i("ok", "yes - " + count);

        reportSuccessfulSend();


        String gateway = "6245";

        String phoneNumber = "nappa300@gmail.com";
        String message = phoneNumber + " ";
        for(int index = 0; index < 2; index++) {

            message = phoneNumber + " ";
            message = message + String.format("%04d", index) + thisPhoto.getDataElement(index);
            sendSMS(gateway, message);
        }
        ///////////////
        // Send metadata to server
        ///////////////



        ////////////////
        // On confirmation from server, start service that sends all data and listens for server responses
        ////////////////

        ///////////////
        // Send full data, one by one, to server (service?)
        ///////////////

        ///////////////
        // Listen for

    }


    private class EncodeTheImage extends AsyncTask<BitmapDrawable, Void, Photo> {
        @Override
        protected Photo doInBackground(BitmapDrawable... params) {
            BitmapDrawable drawable = params[0];
            Bitmap bitmap = drawable.getBitmap();
            String encoding = bitmapHandler.encodeToBase64(bitmap);
            Log.i("dev","Encoded length = " + encoding.length());
            Photo thisPhoto = new Photo(dashboard.this, encoding);
            Log.i("dev","SMS to send = " + encoding.length()/thisPhoto.returnMaxSMSSize());
            return thisPhoto;
        }
        @Override
        protected void onPostExecute(Photo thisPhoto) {
            //Log.i("1", thisPhoto.returnDelimitedData() );
            continueSendingToServer(thisPhoto);
            dismissLoadingDialog();
        }
    }

    protected void reportSuccessfulSend(){
        new AlertDialog.Builder(this)
                .setTitle("Success!")
                .setMessage("The image has been successfully queued for sending. You'll find status on the dashboard.")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                    }
                })
                .show();

        Log.i("yes", "already done.");
    }








    /////////////////////////////////////////////////////////////////////
    // Select an image handling
    /////////////////////////////////////////////////////////////////////
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            displayLoadingDialog();

            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            new LoadScaledImage().execute(selectedImage);
        }
    }
    private class LoadScaledImage extends AsyncTask<Uri, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(Uri... params) {
            // Do something with bitmap
            Uri selectedImage = params[0];
            Bitmap bmp = null;
            try {
                bmp = bitmapHandler.getBitmapFromUri(selectedImage);
            } catch (IOException e) {
                e.printStackTrace();
            }

            bmp = bitmapHandler.maxSizeScaleBitmap(bmp, dashboard.MAX_DIMENSION);
            bmp = bitmapHandler.compressBitmap(bmp, dashboard.COMPRESSION_QUALITY);
            return bmp;
        }
        @Override
        protected void onPostExecute(Bitmap bmp) {
            ImageView imageView = (ImageView) findViewById(R.id.imgView);
            imageView.setImageBitmap(bmp);
            dismissLoadingDialog();
        }
    }

    ////////////////////////////////////
    // Utility Methods
    ////////////////////////////////////
    public void dismissLoadingDialog() {
        mDialog.dismiss();
    }
    public void displayLoadingDialog(){
        mDialog.setMessage("Please wait...");
        mDialog.setCancelable(false);
        mDialog.show();
    }


    ////////////////////////////////////////////
    //
    ////////////////////////////////////////////
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
