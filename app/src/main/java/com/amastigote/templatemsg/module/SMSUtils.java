package com.amastigote.templatemsg.module;

import android.app.PendingIntent;
import android.telephony.SmsManager;

import java.util.List;

public class SMSUtils {

    public static void snd_msg(String string, String tel, PendingIntent spi) {
        SmsManager sm = SmsManager.getDefault();
        List<String> str_lst = sm.divideMessage(string);
        for (String str : str_lst)
            sm.sendTextMessage(tel, null, str, spi, null);
    }
}
