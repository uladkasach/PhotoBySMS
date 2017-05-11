package com.kasach.photobysms;

import android.content.Context;
import android.text.TextUtils;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by vlad on 11/23/16.
 */

public class SMSHandler {
    private Context CONTEXT;
    public static int MAX_SMS_SIZE = 160;
    public static int MAX_SMS_CONTENT_SIZE = MAX_SMS_SIZE - 17 - 4; //  // reserve 4 character for index of data part

    public SMSHandler(Context context){
        this.CONTEXT = context;
    }


}
