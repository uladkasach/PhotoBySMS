package com.kasach.photobysms;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Base64;
import android.util.Log;
import android.content.Context;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.IOException;

/**
 * Created by vlad on 11/22/16.
 */

public class BitmapHandler {
    public Context theContext;

    public BitmapHandler(Context context){
        theContext = context;
    }


    public String encodeToBase64(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b,Base64.DEFAULT);
        return imageEncoded;
    }

    public Bitmap compressBitmap(Bitmap bitmap, int quality){
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out);
        Bitmap decoded = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));
        return decoded;
    }

    public Bitmap maxSizeScaleBitmap(Bitmap bitmap, int maxDimension) {

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        //int bounding = dpToPx(maxDimension);
        int bounding = (maxDimension);
        //Log.i("Test", "original width = " + Integer.toString(width));
        //Log.i("Test", "original height = " + Integer.toString(height));
        //Log.i("Test", "bounding = " + Integer.toString(bounding));

        // Determine how much to scale: the dimension requiring less scaling is
        // closer to the its side. This way the image always stays inside your
        // bounding box AND either x/y axis touches it.
        float xScale = ((float) bounding) / width;
        float yScale = ((float) bounding) / height;
        float scale = (xScale <= yScale) ? xScale : yScale;
        //Log.i("Test", "xScale = " + Float.toString(xScale));
        //Log.i("Test", "yScale = " + Float.toString(yScale));
        //Log.i("Test", "scale = " + Float.toString(scale));

        if(scale > 1){
            return bitmap; // If its already less than max dimension, youre done.
        }

        // Create a matrix for the scaling and add the scaling data
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);

        // Create a new bitmap and convert it to a format understood by the ImageView
        Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        width = scaledBitmap.getWidth(); // re-use
        height = scaledBitmap.getHeight(); // re-use
        Log.i("Bhan", "scaled width = " + Integer.toString(width));
        Log.i("Bhan", "scaled height = " + Integer.toString(height));

        return scaledBitmap;

    }

    public Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor = theContext.getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();

        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }


    private int dpToPx(int dp) {
        float density = theContext.getApplicationContext().getResources().getDisplayMetrics().density;
        return Math.round((float)dp * density);
    }
}
