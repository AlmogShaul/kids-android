package com.prod.almog.myapplication;

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
                    String shortPhoneNum=Helper.me().getShortPhoneNum(senderNum);
                    if(message.equals("1")){
                        Helper.me().stopSendingSMSNumbers.add(shortPhoneNum);
                        Helper.me().confirmAbsent(shortPhoneNum);
                    }
                    if(message.equals("2")){
                        Helper.me().stopSendingSMSNumbers.remove(shortPhoneNum);
                        Helper.me().clearConfirmAbsent(shortPhoneNum);
                    }


                } // end for loop
            } // bundle is null

        } catch (Exception e) {
            Log.e("SmsReceiver", "Exception smsReceiver" +e);

        }
    }
}