package com.prod.almog.myapplication;

/**
 * Created by shaul.almog on 04/11/2016.
 */
public class Kid {
    Boolean absentConfirmed;
    String id;
    String name;
    String father;
    String mother;
    String fatherPhone;
    String motherPhone;
    Boolean arrived;
    String reminderTime;
    Boolean messageSent;

    public Kid(){
        absentConfirmed = false;
        name = "";
        father = "";
        fatherPhone = "";
        motherPhone = "";
        mother = "";
        arrived = false;
        reminderTime = "10:00";
        messageSent = false;
    }

}
