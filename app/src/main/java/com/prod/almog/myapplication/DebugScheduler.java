//package com.prod.almog.myapplication;
//
//import android.telephony.SmsManager;
//import com.google.android.gms.tasks.Task;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.GregorianCalendar;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.concurrent.Executors;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.TimeUnit;
//
///**
// * Created by shaul.almog on 06/11/2016.
// */
//public class DebugScheduler {
//
//    private static DebugScheduler instance = null;
//    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
//
//    public static DebugScheduler me() {
//        if (instance == null) {
//            instance = new DebugScheduler();
//        }
//        return instance;
//    }
//
//    private DebugScheduler() {
//    }
//
//    private ArrayList<Kid> kids = new ArrayList<>();
//    private ArrayList<Date> holidays = new ArrayList<>();
//    private Boolean dayZero = true;
//
//    public void start(ArrayList<Kid> _kids, ArrayList<Date> _holidays) {
//        kids = new ArrayList<>();
//        holidays = new ArrayList<>();
//        holidays.addAll(_holidays);
//        kids.addAll(_kids);
//    }
//
//
//    public void startSchedule() {
//        scheduleNotifications();
//    }
//
//
//
//    public void scheduleNotifications() {
//        if (!isHoliday()) {
//            for (Kid kid : kids) {
//                scheduleKidNotification(kid);
//            }
//        }
//    }
//
//    public void clearEntrances() {
//
//
//    }
//
//
//
//    private void clearKidExecutors() {
//        for (ScheduledExecutorService service : kidExecutors) {
//            service.shutdown();
//        }
//        kidExecutors.clear();
//    }
//
//    private ArrayList<ScheduledExecutorService> kidExecutors = new ArrayList<ScheduledExecutorService>();
//
//    private void scheduleKidNotification(final Kid _kid) {
//        final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
//        kidExecutors.add(executorService);
//
//        Runnable run = new Runnable() {
//            @Override
//            public void run() {
//                if (!isNotifyTimeValid()) return;
//                boolean timePassed = isTimePassed(_kid);
//                boolean willSendSMS = willSendSMS(_kid, timePassed);
//                if (willSendSMS) {
//                    sendSMS(_kid);
//                }
//            }
//
//            private boolean isNotifyTimeValid() {
//                try {
//                    Integer.parseInt(_kid.reminderTime.replace(":", ""));
//                } catch (Exception e) {
//                    KidsLogger.error(_kid.name + "notify time: " + _kid.reminderTime + "not valid");
//                    return false;
//                }
//                return true;
//            }
//        };
//        executorService.scheduleAtFixedRate(run, 0, Long.parseLong(Manager.me().settings.get("notificationPeriod")), TimeUnit.MINUTES);
//    }
//
//    private boolean isHoliday() {
//        boolean sameDay = false;
//        Calendar cal1 = Calendar.getInstance();
//        Calendar cal2 = Calendar.getInstance();
//        cal1.setTime(new Date());
//        for (Date date : holidays) {
//            cal2.setTime(date);
//            sameDay = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
//                    cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
//        }
//        return sameDay;
//    }
//
//
//    private boolean willSendSMS(Kid _kid, boolean timePassed) {
//        return !_kid.arrived && !_kid.absentConfirmed && timePassed ;
//    }
//
//    private boolean isTimePassed(Kid _kid) {
//        Integer kidReminderTimeInt = 0;
//        kidReminderTimeInt = Integer.parseInt(_kid.reminderTime.replace(":", ""));
//        Integer nowTimeInt = getNowTimeInt();
//        return kidReminderTimeInt < nowTimeInt;
//    }
//
//    private Integer getNowTimeInt() {
//        Date date = new Date();   // given date
//        Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
//        calendar.setTime(date);   // assigns calendar to given date
//        String hour = String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
//        String min = String.valueOf(calendar.get(Calendar.MINUTE));
//        if (min.length() == 1) {
//            min = "0" + min;
//        }
//        return Integer.parseInt(hour + min);
//    }
//
//    private void sendSMS(Kid kid) {
//        String notifyMessage = Manager.me().settings.get("notifyMessage");
//        String message = String.format(notifyMessage, kid.name);
//        String stopSMS = Manager.me().settings.get("stopSMS");
//        if (stopSMS == null) stopSMS = "false";
//        if (stopSMS.equals("true")) {
//            Manager.me().toast(message);
//        } else {
//            SmsManager smsManager = SmsManager.getDefault();
//            if(!Manager.me().stopSendingSMSNumbers.contains(kid.fatherPhone)) {
//                try {
//                    smsManager.sendTextMessage(kid.fatherPhone, null, message, null, null);
//                }
//                catch (Exception e){e.toString();}
//            }
//            if(!Manager.me().stopSendingSMSNumbers.contains(kid.motherPhone)){
//                try {
//                    smsManager.sendTextMessage(kid.motherPhone, null, message, null, null);
//                }
//                catch (Exception e){e.toString();}
//            }
//
//        }
//        kid.messageSent = true;
//    }
//
//    public void clearWorkers() {
//        clearKidExecutors();
//    }
//}
