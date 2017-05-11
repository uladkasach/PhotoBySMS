package com.kasach.photobysms;

import android.content.Context;
import android.text.TextUtils;

/**
 * Created by vlad on 11/23/16.
 */

public class Photo {
    private Context CONTEXT;
    private int MAX_SMS_SIZE;
    private String[] dataArray;
    private String delimitedData;

    public Photo(Context context, String photoData){
        CONTEXT = context;
        SMSHandler smsHandler = new SMSHandler(this.CONTEXT);
        MAX_SMS_SIZE = smsHandler.MAX_SMS_CONTENT_SIZE;
        loadPhotoData(photoData);
    }

    public int returnMaxSMSSize(){
        return MAX_SMS_SIZE;
    }
    public String returnDelimitedData(){
        return delimitedData;
    }
    public String getDataElement(int index){
        return dataArray[index];
    }

    public void loadPhotoData(String data){
        if(data.contains("||")){
            loadDelimitedEncodingData(data);
        }else {
            loadRawEncodingData(data);
        }
    }

    private void loadRawEncodingData(String data){
        int length = data.length();
        int parts = length/MAX_SMS_SIZE;
        String sub;
        int start;
        int end;
        String[] stringArray = new String[parts];
        for(int i=0; i<parts; i++){
            start = i*MAX_SMS_SIZE;
            end = (i+1)*MAX_SMS_SIZE;
            sub = data.substring(start,end);
            stringArray[i] = sub;
        }
        this.dataArray = stringArray;
        this.delimitedData = TextUtils.join("||", stringArray);
    }

    private void loadDelimitedEncodingData(String data){
        this.delimitedData = data;
        this.dataArray = data.split("||");
    }

}
