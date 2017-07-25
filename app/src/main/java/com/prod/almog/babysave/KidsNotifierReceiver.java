package com.prod.almog.babysave;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

public class KidsNotifierReceiver extends BroadcastReceiver {

    public HashMap<String, String> settings = new HashMap<>();
    FirebaseService firebaseService = new FirebaseService();
    ArrayList<Date> holidays = new ArrayList<>();

    private boolean isHoliday() {
        try {
            boolean sameDay = false;
            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            cal1.setTime(new Date());
            if(cal1.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)
                return  true;
            else {
                for (Date date : holidays) {
                    cal2.setTime(date);
                    sameDay = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
                    if (sameDay == true) return sameDay;
                }
                return sameDay;
            }
        }catch (Exception e){
            return  false;
        }
    }

    private void parseHolidays() {
        holidays.clear();
        String _holidays = settings.get("holidays");
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        if (_holidays != null) {
            String[] arr = _holidays.split(";");
            for (String item : arr) {
                try {
                    holidays.add(formatter.parse(item));
                } catch (ParseException e) {
                    Manager.me().log("ERROR", "שגיאה בניתוח יום חופשה" + e.getMessage());
                }
            }
        }
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        Manager.me().setContext(context);

        firebaseService.getSettings(new IResult<HashMap<String, String>>() {
            @Override
            public void accept(HashMap<String, String> stringStringHashMap) {
                settings = stringStringHashMap;
                parseHolidays();
                if (!isHoliday()) {

                    firebaseService.getKids(new IResult<ArrayList<Kid>>() {
                        @Override
                        public void accept(ArrayList<Kid> kids) {
                            NotifyRelevantKids(kids);
                        }
                    });
                }
            }
        });


    }

    private void NotifyRelevantKids(ArrayList<Kid> kids) {
        for (Kid kid : kids) {
            boolean willSendSMS = willSendSMS(kid);
            if (willSendSMS) {
                sendSMS(kid);
            }
        }
    }


    private boolean willSendSMS(Kid _kid) {
        return !_kid.arrived &&
                !_kid.absentConfirmed &&
                !isOnVacation(_kid) &&
                isTimePassed(_kid)  &&
                !Manager.me().passesNotificationHours();
    }

    private boolean isTimePassed(Kid _kid) {
        try {
            Integer kidReminderTimeInt;
            kidReminderTimeInt = Integer.parseInt(_kid.reminderTime.replace(":", ""));
            Integer nowTimeInt = getNowTimeInt();
            return kidReminderTimeInt < nowTimeInt;
        } catch (Exception e) {
            Manager.me().log("ERROR", "שגיאה במציאת אם הזמן עבר" + e.getMessage());
            return false;
        }
    }

    private boolean isOnVacation(Kid _kid){
        try {
            Date now = new Date();
            if(_kid.vacationPeriodFrom != null && _kid.vacationPeriodTo != null){
                if((now.getTime() > _kid.vacationPeriodFrom.getTime()) && (now.getTime() < _kid.vacationPeriodTo.getTime())){
                    return true;
                }else{
                    return false;
                }
            }else{
                return false;
            }

        } catch (Exception e) {
            Manager.me().log("ERROR", "שגיאה במציאת אם הזמן עבר" + e.getMessage());
            return false;
        }

    }

    private Integer getNowTimeInt() {
        Date date = new Date();   // given date
        Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
        calendar.setTime(date);   // assigns calendar to given date
        String hour = String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
        String min = String.valueOf(calendar.get(Calendar.MINUTE));
        if (min.length() == 1) {
            min = "0" + min;
        }
        return Integer.parseInt(hour + min);
    }

    private void sendSMS(Kid kid) {
        String notifyMessage = Manager.me().settings.get("notifyMessage");
        String message = String.format(notifyMessage, kid.name);
        String stopSMS = Manager.me().settings.get("stopSMS");
        if (stopSMS == null) stopSMS = "false";
        if (stopSMS.equals("true")) {
            Manager.me().toast(message);
        } else {
            SmsManager smsManager = SmsManager.getDefault();
            if (!kid.absentConfirmed) {

                try {
                    if (kid.fatherPhone.length() > 5) {
                        ArrayList<String> messageParts = smsManager.divideMessage(message);
                        for(String part : messageParts)
                        {
                            smsManager.sendTextMessage(kid.fatherPhone, null, part, null, null);
                        }
                    }
                } catch (Exception e) {
                    Manager.me().log("ERROR", "שגיאה בשליחת SMS" + e.getMessage() + "מספר" + kid.fatherPhone);
                }
                try {
                    if (kid.motherPhone.length() > 5) {
                        ArrayList<String> messageParts = smsManager.divideMessage(message);
                        for(String part : messageParts)
                        {
                            smsManager.sendTextMessage(kid.motherPhone, null, part, null, null);
                        }
                    }
                } catch (Exception e) {
                    Manager.me().log("ERROR", "שגיאה בשליחת SMS" + e.getMessage() + "מספר" + kid.motherPhone);
                }

                kid.messageSent = true;
            }
        }
    }
}