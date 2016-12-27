package com.prod.almog.myapplication;

import android.telephony.SmsManager;
import android.widget.CursorAdapter;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.LogManager;

/**
 * Created by shaul.almog on 06/11/2016.
 */
public class Scheduler {

    private static Scheduler instance = null;
    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
    public static Scheduler me() {
        if(instance == null) {
            instance = new Scheduler();
        }
        return instance;
    }

    private Scheduler(){}

    SmsManager smsManager = SmsManager.getDefault();
    private ArrayList<Kid> kids = new ArrayList<>();

    public void start(ArrayList<Kid> _kids){
        kids.addAll(_kids);
        startScheduleDay();
    }


    private Date getFirstEarlyMorning(){
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        return new GregorianCalendar(year, month, day,7,0).getTime();
    }

    public void startScheduleDay(){
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {

            synchronized public void run() {
                executorService.shutdown();
                executorService= Executors.newScheduledThreadPool(1);
                executorService.schedule(new Runnable() {
                    @Override
                    public void run() {
                        for (Kid kid:kids) {
                            kid.arrived = false;
                            NotifyKidWhenNeeded(kid);
                        }
                    }
                },1, TimeUnit.DAYS);
            }

        },getFirstEarlyMorning(), 0);

    }


    Calendar calendar = Calendar.getInstance();

    private Date getKidReminderTime(Kid _kid) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        return simpleDateFormat.parse(_kid.reminderTime);
    }

    private Date getNow(){
        return new Date();

        //TODO check if needed
//        SimpleDateFormat simpleDateFormat =  new SimpleDateFormat("HH:mm");
//        String minute = String.valueOf( calendar.get(Calendar.MINUTE));
//        String hour = String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
//        String time = hour + ":" + minute;
//        Date nowTime = null;
//        return simpleDateFormat.parse(time);

    }

    private void NotifyKidWhenNeeded(final Kid _kid){
        final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        Runnable run = new Runnable() {
            @Override
            public void run() {
                Date kidReminderTime = null;
                try {
                    kidReminderTime = getKidReminderTime(_kid);
                } catch (ParseException e) {
                    KidsLogger.error(_kid.name + "notify time: " + _kid.reminderTime + "not valid");
                    return;
                }
                Date nowTime = getNow();
                boolean timePassed = nowTime.compareTo(kidReminderTime) > 0 ;
                boolean willSendSMS ;
                willSendSMS =!_kid.arrived && timePassed && !_kid.messageSent;
                if(willSendSMS){
                    sendSMS(_kid);
                }
                else{
                    executorService.shutdown();
                }
            }
        };
        executorService.schedule(run,1, TimeUnit.MINUTES);
    }

    private void sendSMS(Kid kid) {
        String message =kid.name +" לא הגיע היום לגן.";
        Helper.me().toast(message);
//        smsManager.sendTextMessage(kid.fatherPhone, "0000000", message, null, null);
        kid.messageSent = true;
    }
}
