package com.prod.almog.babysave;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

public class IncomingSms extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {

        // Retrieves a map of extended data from the intent.
        final Bundle bundle = intent.getExtras();

        try {

            if (bundle != null) {

                final Object[] pdusObj = (Object[]) bundle.get("pdus");

                for (int i = 0; i < pdusObj.length; i++) {

                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
                    String phoneNumber = currentMessage.getDisplayOriginatingAddress();

                    String senderNum = phoneNumber;
                    String message = currentMessage.getDisplayMessageBody();
                    String shortPhoneNum=Manager.me().getShortPhoneNum(senderNum);
                    if(message.equals("1")){
                        Manager.me().confirmAbsent(shortPhoneNum);
                    }
                    if(message.equals("2")){
                        Manager.me().clearConfirmAbsent(shortPhoneNum);
                    }


                } // end for loop
            } // bundle is null

        } catch (Exception e) {
            Manager.me().log("ERROR", "שגיאה בקבלת SMS"+e.getMessage());

        }
    }
}