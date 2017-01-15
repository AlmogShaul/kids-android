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
    private ArrayList<Date> holidays = new ArrayList<>();

    private Scheduler(){}

    SmsManager smsManager = SmsManager.getDefault();
    private ArrayList<Kid> kids = new ArrayList<>();

    public void start(ArrayList<Kid> _kids,ArrayList<Date> _holidays){
        holidays.addAll(_holidays);
        kids.addAll(_kids);
        startSchedule();
    }

    private boolean isHoliday() {
        boolean sameDay = false;
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(new Date());
        for (Date date : holidays) {
            cal2.setTime(date);
            sameDay = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                    cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
        }
        return sameDay;
    }

    public void startSchedule(){
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            synchronized public void run() {
                startDayScheduleTask();
            }
        },getFirstEarlyMorning());
    }

    private void clearKidExecutors() {
        for(ScheduledExecutorService service : kidExecutors){
            service.shutdown();
        }
        kidExecutors.clear();
    }


    private void startDayScheduleTask() {
        executorService.shutdown();
        executorService= Executors.newScheduledThreadPool(100);
        executorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                if(!isHoliday()) {
                    for (Kid kid : kids) {
                        kid.arrived = false;
                        scheduleKidNotification(kid);
                    }
                }
            }
        },0,1, TimeUnit.DAYS);
    }

    private ArrayList<ScheduledExecutorService> kidExecutors = new ArrayList<ScheduledExecutorService>();

    private void scheduleKidNotification(final Kid _kid){
        final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        kidExecutors.add(executorService);

        Runnable run = new Runnable() {
            @Override
            public void run() {
                if (!isNotifyTimeValid()) return;
                boolean timePassed = isTimePassed(_kid);
                boolean willSendSMS = willSendSMS(_kid,timePassed);
                if(willSendSMS){
                    sendSMS(_kid);
                }
            }
            private boolean isNotifyTimeValid() {
                try {
                    Integer.parseInt(_kid.reminderTime.replace(":", ""));
                }
                catch (Exception e){
                    KidsLogger.error(_kid.name + "notify time: " + _kid.reminderTime + "not valid");
                    return false;
                }
                return true;
            }
        };
        executorService.scheduleAtFixedRate(run,0,1, TimeUnit.MINUTES);
    }

    private boolean willSendSMS(Kid _kid,boolean timePassed){
        return !_kid.arrived && timePassed && !_kid.messageSent;
    }

    private boolean isTimePassed(Kid _kid) {
        Integer kidReminderTimeInt = 0;
        kidReminderTimeInt = Integer.parseInt(_kid.reminderTime.replace(":", ""));
        Integer nowTimeInt = getNowTimeInt();
        return kidReminderTimeInt < nowTimeInt;
    }

    private Date getFirstEarlyMorning() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        Date date = new GregorianCalendar(year, month, day, 7, 0).getTime();
        if(new Date().before(date)) {
            return date;
        }
        else{
            GregorianCalendar gregorianCalendar = new GregorianCalendar(year, month, day, 7, 0);
            gregorianCalendar.add(Calendar.DATE,1);
            return gregorianCalendar.getTime();
        }
    }

    private Integer getNowTimeInt(){
        Date date = new Date();   // given date
        Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
        calendar.setTime(date);   // assigns calendar to given date
        String hour = String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
        String min =  String.valueOf(calendar.get(Calendar.MINUTE));
        return  Integer.parseInt(hour+min);
    }

    private void sendSMS(Kid kid) {
        String message =kid.name +" לא הגיע היום לגן.";
        String stopSMS = Helper.me().settings.get("stopSMS");
        if(stopSMS == null) stopSMS = "false";
        if(stopSMS.equals("true")) {
            smsManager.sendTextMessage(kid.fatherPhone, null, message, null, null);
        }
        kid.messageSent = true;
    }
}
