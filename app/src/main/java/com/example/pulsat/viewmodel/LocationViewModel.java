package com.example.pulsat.viewmodel;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.databinding.BaseObservable;

import com.example.pulsat.activity.MainActivity;
import com.example.pulsat.activity.SettingsActivity;
import com.example.pulsat.event.RequestAccesFineLocationPermissionEvent;
import com.example.pulsat.event.StartInterventionEvent;
import com.example.pulsat.event.StopInterventionEvent;
import com.example.pulsat.foreground.ForegroundLocation;
import com.example.pulsat.foreground.ForegroundLocationOld;

import org.greenrobot.eventbus.EventBus;

public class LocationViewModel {
    private final Context context;
    public LocationViewModel(Context context){
        this.context = context;
    }

    public void startLocation(boolean isForegroundLocationRunning){
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // Permission is already available, start foregroundLocation
            startForegroundService(isForegroundLocationRunning);

        } else {
            // Permission is missing and must be requested.
            EventBus.getDefault().post(new RequestAccesFineLocationPermissionEvent());
        }
    }
    private void startForegroundService(boolean isForegroundLocationRunning){
        if (!isForegroundLocationRunning) {
            Intent startIntent = new Intent(context, ForegroundLocation.class);
            startIntent.setAction(ForegroundLocation.ACTION_START_FOREGROUND_SERVICE);
            // Call startService with Intent parameter.
            context.startForegroundService(startIntent);
            Log.d("LOCATIONVIEWMODEL", "Launch Foreground");
        }
    }
    public void stopForegroundService(){
        Intent stopIntent = new Intent(context, ForegroundLocation.class);
        stopIntent.setAction(ForegroundLocation.ACTION_STOP_FOREGROUND_SERVICE);
        context.startForegroundService(stopIntent);
    }
    public void manualStartIntervention(){
        EventBus.getDefault().post(new StartInterventionEvent());
    }
    public void manualStopIntervention(){
        EventBus.getDefault().post(new StopInterventionEvent());
    }
}

