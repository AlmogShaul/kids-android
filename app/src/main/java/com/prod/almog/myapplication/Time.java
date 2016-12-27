package com.prod.almog.myapplication;

/**
 * Created by shaul.almog on 06/11/2016.
 */
public class Time {
    public Time(int _hour,int _min){
        hour = _hour;
        minute = _min;
    }
    public String toString(){
        return String.valueOf(hour) + ':' + String.valueOf(minute);
    }
    int hour;
    int minute;
    int second;
}
