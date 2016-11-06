package com.amastigote.templatemsg.module;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

public class PermissionUtils {
    /*
        this method could be static
     */
    public static void chk_pem(Context context, Activity activity) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            int SMS_PEM_CODE = 0;
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.SEND_SMS},
                    SMS_PEM_CODE);
        } else if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            int CON_PEM_CODE = 2;
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_CONTACTS},
                    CON_PEM_CODE);
        }
    }
}
