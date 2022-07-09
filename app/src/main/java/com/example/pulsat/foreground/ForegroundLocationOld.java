package com.example.pulsat.foreground;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LifecycleService;

import com.example.pulsat.R;
import com.example.pulsat.event.ForegroundLocationStateEvent;
import com.example.pulsat.event.InterventionStateEvent;
import com.example.pulsat.event.StartInterventionEvent;
import com.example.pulsat.event.StopInterventionEvent;
import com.example.pulsat.model.Rdv;

import com.example.pulsat.viewmodel.RdvViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ForegroundLocationOld extends LifecycleService {

    private static final String TAG = "TAG_FOREGROUND";
    public static final String ACTION_START_FOREGROUND_SERVICE = "ACTION_START_FOREGROUND_SERVICE";
    public static final String ACTION_STOP_FOREGROUND_SERVICE = "ACTION_STOP_FOREGROUND_SERVICE";
    private static final int GPS_REFRESH_INTERVAL = 2;
    private LocationCallback locationCallback;
    private FusedLocationProviderClient fusedLocationClient;
    private Rdv intervention;
    private Rdv lastRdv;
    private RdvViewModel rdvViewModel;
    private Boolean bluetoothConnectionState = false;
    private Boolean flagNewLocation = true;
    private Map<String, Integer> listOfAdress;
    final static int NOTIFICATION_ID = 12; // valeur arbitraire
    private String address;
    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            final String action = intent.getAction();
            Log.d("TAG_FOREGROUND_BROADCASTRECEIVER",  "Action: " + action);


            if (action.equals(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)) {

                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, BluetoothAdapter.ERROR);
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                //EventBus.getDefault().post(new RequestBluetoothConnectPermissionEvent());
                //stopForegroundService();
                //return;
                //}
                @SuppressLint("MissingPermission")
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                Log.d("TAG_FOREGROUND_BROADCASTRECEIVER", deviceName + "/" + deviceHardwareAddress);

                if (deviceHardwareAddress.equals("E4:04:39:F2:EC:68")) {            // camion
                    //if (deviceHardwareAddress.equals("3E:AF:36:17:F8:BC")) {          // enceinte
                    switch (state) {
                        case BluetoothAdapter.STATE_CONNECTED:
                            if (!bluetoothConnectionState) {
                                terminateIntervention();
                                bluetoothConnectionState = true;
                            }
                            break;
                        case BluetoothAdapter.STATE_DISCONNECTED:
                            if (bluetoothConnectionState ) {
                                createIntervention();
                                bluetoothConnectionState = false;
                            }
                            break;
                    }
                }
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "OnStartCommand");
        rdvViewModel.getCurrentRdv().observe(this, rdv -> {
            if (rdv != null) {
                Log.d("TAG_FOREGROUND_SERVICE", "Debut :" + rdv.getArrival());
                lastRdv = rdv;
            }
        });

        if (intent != null) {
            String action = intent.getAction();

            switch (action) {
                case ACTION_START_FOREGROUND_SERVICE:
                    startForegroundService();
                    fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
                    createLocationRequest();
                    break;
                case ACTION_STOP_FOREGROUND_SERVICE:
                    stopForegroundService();
                    break;
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "OnCreate");
        listOfAdress = new HashMap<>();
        super.onCreate();
        rdvViewModel = new RdvViewModel(getApplication());
        IntentFilter filter1 = new IntentFilter(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        filter1.addAction(Context.NOTIFICATION_SERVICE);
        registerReceiver(mBroadcastReceiver1, filter1);

        //setStopLocationCheckPoint();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (!bluetoothConnectionState) {
                        address = geocoder(location.getLongitude(), location.getLatitude());
                        address = getRealAdress(address, flagNewLocation);
                        if (intervention != null) {
                            intervention.setAddress(address);
                            rdvViewModel.updateRdv(intervention);
                        } else {
                            Log.d(TAG, "Long.:" + location.getLongitude() + " Lat.:" + location.getLatitude());
                        }
                        flagNewLocation = false;
                    } else {
                        flagNewLocation = true;
                    }
                    Log.d("TAG_FOREGROUND_SERVICE", "Adresse :" + address);

                }
            }
        };

    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "OnDestroy");
        super.onDestroy();
        fusedLocationClient.removeLocationUpdates(locationCallback);
        unregisterReceiver(mBroadcastReceiver1);
    }
    private String createNotificationChannel(NotificationManager notificationManager){
        String channelId = "my_service_channelid";
        String channelName = "My Foreground Service";
        NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);

        channel.setImportance(NotificationManager.IMPORTANCE_NONE);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        notificationManager.createNotificationChannel(channel);
        return channelId;
    }

    private void startForegroundService(){
        Log.d(TAG, "startForegroundSrevice");
        EventBus.getDefault().register(this);
        // Create the Foreground Service
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = createNotificationChannel(notificationManager);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setContentText("Localisation activée")
                .build();

        startForeground(NOTIFICATION_ID, notification);
        EventBus.getDefault().postSticky(new ForegroundLocationStateEvent(true));
    }

    private void stopForegroundService(){
        Log.d(TAG, "stopForegroundService");
        EventBus.getDefault().unregister(this);
        // Stop foreground service and remove the notification.
        terminateIntervention();
        EventBus.getDefault().postSticky(new ForegroundLocationStateEvent(false));
        stopForeground(true);
        // Stop the foreground service.
        stopSelf();
    }
    @SuppressLint("MissingPermission")
    protected void createLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000*60*GPS_REFRESH_INTERVAL);
        locationRequest.setFastestInterval(1000*60*GPS_REFRESH_INTERVAL/2);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());
    }
    private String geocoder(double longitude, double latitude)  {
        Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            return address;
        } catch (IOException ioException) {
            Log.e(TAG, "Service Not Available", ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            Log.e(TAG, "Invalid Latitude or Longitude Used" + ". " +
                    "Latitude = " + latitude + ", Longitude = " +
                    longitude, illegalArgumentException);
        }
        return null;
    }
    private String getRealAdress(String adress,Boolean flagNewLocation){

        if (flagNewLocation){
            listOfAdress = new HashMap<>();
        }
        Integer val = listOfAdress.get(adress);
        if (val != null){
            listOfAdress.put(adress,++val);
        }else{listOfAdress.put(adress,1);};
        Log.d(TAG, "getRealAdressEntryset: "+listOfAdress.entrySet());
        Iterator<Map.Entry<String, Integer>> iterator = listOfAdress.entrySet().iterator();
        int valMax = 0;
        while (iterator.hasNext()){
            Map.Entry mapentry = (Map.Entry) iterator.next();

            if ((int)mapentry.getValue()>valMax){
                adress = mapentry.getKey().toString();
                valMax = (int) mapentry.getValue();
                Log.d(TAG, "getRealValMax: "+adress+"="+valMax);
            }

        }
        return adress;
    }
    private void terminateIntervention(){
        Log.d(TAG, "terminateIntervention");
        if (intervention != null) {
            Log.d(TAG, "terminateIntervention2: " + address);
            intervention.setElapsedTime((System.currentTimeMillis() - intervention.getArrival()) / 1000);
            intervention.setAddress(address);
            rdvViewModel.updateRdv(intervention);
            if (bluetoothConnectionState)
                rdvViewModel.deleteRdv(intervention);
            intervention = null;
        }
        address = "Pas encore, d'adresse";
        EventBus.getDefault().postSticky(new InterventionStateEvent(false));

    }
    private void createIntervention(){
        Log.d(TAG, "CreateIntervention: "+address);
        intervention = new Rdv(System.currentTimeMillis());
        intervention.setAddress(address);
        if (lastRdv != null)
            if (lastRdv.getElapsedTime() == 0)
                updateIntervention();

        rdvViewModel.insertRdv(intervention);
        EventBus.getDefault().postSticky(new InterventionStateEvent(true));
    }
    private void updateIntervention(){
        Log.d(TAG, "UpdateIntervention: ");
        intervention.setArrival(lastRdv.getArrival());
        intervention.setAddress(lastRdv.getAddress());
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onStartInterventionEvent(StartInterventionEvent event){
        if (intervention==null)
        createIntervention();
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onStopInterventionEvent(StopInterventionEvent event){
        terminateIntervention();
    }

    private void setStopLocationCheckPoint(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 13);
        calendar.set(Calendar.MINUTE, 51);
        calendar.set(Calendar.SECOND, 0);

        Intent MyIntent = new Intent(getApplicationContext(), ForegroundLocationOld.class);
        PendingIntent MyPendIntent = PendingIntent.getBroadcast(getApplicationContext(), 100,
                MyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager MyAlarm = (AlarmManager) getSystemService(ALARM_SERVICE);
        MyAlarm.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), MyPendIntent);
    }
}
