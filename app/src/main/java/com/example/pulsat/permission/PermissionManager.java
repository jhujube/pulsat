package com.example.pulsat.permission;

import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionManager {
    public static PermissionStatus getPermissionStatus(@NonNull Activity activity,@NonNull String permission){
        if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(activity,permission)){
            return PermissionStatus.PERMISSION_GRANTED;
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(activity,permission)){
            return PermissionStatus.CAN_ASK_PERMISSION;
        } else {
            return PermissionStatus.PERMISSION_DENIED;
        }
    }
}
