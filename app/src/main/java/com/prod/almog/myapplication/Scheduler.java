package com.prod.almog.myapplication;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by shaul.almog on 06/11/2016.
 */
public class Scheduler {


    private Time time ;
    private ArrayList<Kid> kids = new ArrayList<>();

    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);


    public Scheduler(Time _time){
        time = _time;

    }

    public void Schedule(ArrayList<Kid> _kids){
        kids.addAll(_kids);
    }


    ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    public void start(){
        Runnable run = new Runnable() {
            @Override
            public void run() {
                for (Kid kid:kids) {sendSMSToParents(kid);}
            }
        };
        long delay = computeNextDelay(time.hour, time.minute);
        executorService.schedule(run,1, TimeUnit.DAYS);

    }

    private long computeNextDelay(int targetHour, int targetMin)
    {

        ///TODO: Caclulate next delay
//        DateTime localNow = DateTime.now();
//        ZoneId currentZone = ZoneId.systemDefault();
//        ZonedDateTime zonedNow = ZonedDateTime.of(localNow, currentZone);
//        ZonedDateTime zonedNextTarget = zonedNow.withHour(targetHour).withMinute(targetMin).withSecond(targetSec);
//        if(zonedNow.compareTo(zonedNextTarget) > 0)
//            zonedNextTarget = zonedNextTarget.plusDays(1);
//
//        Duration duration = Duration.between(zonedNow, zonedNextTarget);
//        return duration.getSeconds();

        return 1000*60*24;
    }


    private void sendSMSToParents(Kid _kid){

    }
}
