package com.kasach.photobysms;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.icu.text.SimpleDateFormat;
import android.util.Log;

import java.util.Date;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by vlad on 11/22/16.
 */

public class DatabaseHandler {
    private SQLiteDatabase DATABASE;
    private static String PHOTOS_TABLE = "Photos";
    private Context CONTEXT;

    public DatabaseHandler(Context context){
        this.CONTEXT = context;
        this.DATABASE = this.CONTEXT.openOrCreateDatabase("PhotoBySMS", MODE_PRIVATE, null);

        DATABASE.execSQL("DROP TABLE " + PHOTOS_TABLE + ";");
        DATABASE.execSQL("CREATE TABLE IF NOT EXISTS " + PHOTOS_TABLE + "(AutoID INTEGER PRIMARY KEY, Completed INTEGER, Url VARCHAR, DelimitedData VARCHAR);");
    }

    public Boolean saveNewPhoto(Photo newPhoto){

        ContentValues insertValues = new ContentValues();
        insertValues.put("Completed", 0);
        insertValues.put("DelimitedData",  newPhoto.returnDelimitedData());
        long row_id = DATABASE.insert(PHOTOS_TABLE, null, insertValues);

        return row_id != -1; // return false if row_id == -1, meaning there was an error
    }

    public int returnCountOf(String... countWhat){
        int count = 0;

        if(countWhat.length == 0){
            // Of all photos
            long long_count  = DatabaseUtils.queryNumEntries(DATABASE, PHOTOS_TABLE);
            count = (int) long_count;
            DATABASE.close();
            //Log.i("db", count + " < - count");

        } else if(countWhat[0] == "IN_PROGRESS") {
            Log.i("DBMan","return count of inprogress photos");
            Cursor mCount= DATABASE.rawQuery("select count(*) from " + PHOTOS_TABLE +" where Completed='0'", null);
            mCount.moveToFirst();
            count= mCount.getInt(0);
            mCount.close();
        }

        return count;
    }


        /*
        Cursor resultSet = mydatabase.rawQuery("Select * from Photos",null);
        resultSet.moveToFirst();
        String username = resultSet.getString(1);
        String password = resultSet.getString(2);
        Log.i("db", username + " " + password );
        */

}
