package com.prod.almog.myapplication;

import java.util.ArrayList;

/**
 * Created by shaul.almog on 23/11/2016.
 */
public class Kindergarden {
    String id;
    String name;
    String phone;
    String serial;
    ArrayList<String> kidIds = new ArrayList<>();
    public String absenceConfirmedPhones;

    @Override
    public String toString() {
        return name;
    }

}
