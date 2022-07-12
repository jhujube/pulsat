package com.example.pulsat.foreground;

import android.annotation.SuppressLint;
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
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LifecycleService;

import com.example.pulsat.R;
import com.example.pulsat.activity.MainActivity;
import com.example.pulsat.event.ForegroundLocationStateEvent;
import com.example.pulsat.event.InterventionStateEvent;
import com.example.pulsat.event.ReloadSettingsEvent;
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
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ForegroundLocation extends LifecycleService {
    private static final String TAG = "TAG_FOREGROUND";
    public static final String ACTION_START_FOREGROUND_SERVICE = "ACTION_START_FOREGROUND_SERVICE";
    public static final String ACTION_STOP_FOREGROUND_SERVICE = "ACTION_STOP_FOREGROUND_SERVICE";
    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    private static final String NO_ADRESS = "pas encore,d'adresse";
    private static final long MIN_STOP_TIME = 1*60*1000;   // temps en millisecondes
    private static final int NB_SAME_ADDRESS_IS_OK = 5;
    final static int NOTIFICATION_ID = 12; // valeur arbitraire
    private static final int GPS_REFRESH_INTERVAL = 1;
    private LocationCallback locationCallback;
    private FusedLocationProviderClient fusedLocationClient;
    private RdvViewModel rdvViewModel;
    private Rdv intervention;
    private Rdv lastRdv;
    private Boolean isInterventionOngoing = false;
    private Boolean isAddressOk = false;
    private String adress = NO_ADRESS;
    private Map<String, Integer> listOfAdress ;
    private Notification notification;
    private Handler notifHandler;
    private Runnable notificationCode;
    private String btDevice;

    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            final String action = intent.getAction();
                if (action.equals(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)) {

                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, BluetoothAdapter.ERROR);
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                String deviceHardwareAddress = device.getAddress(); // MAC address
                    if (deviceHardwareAddress.equals(getBtDeviceMac(btDevice))) {
                    //if (deviceHardwareAddress.equals("E4:04:39:F2:EC:68")) {            // camion
                    //if (deviceHardwareAddress.equals("3E:AF:36:17:F8:BC")) {          // enceinte
                    switch (state) {
                        case BluetoothAdapter.STATE_CONNECTED:
                            if (isInterventionOngoing)
                                closeIntervention();
                        break;

                        case BluetoothAdapter.STATE_DISCONNECTED:
                            if (!isInterventionOngoing)
                                createNewIntervention();
                        break;
                    }
                }
            }
        }
    };
    @Override
    public void onCreate() {
        super.onCreate();
        loadSettings();
        notifHandler = new Handler();
        notificationHandler();
        rdvViewModel = new RdvViewModel(getApplication());
        IntentFilter filter1 = new IntentFilter(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver1, filter1);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {

                for (Location location : locationResult.getLocations()) {
                    Log.d(TAG, "Long.:" + location.getLongitude() + " Lat.:" + location.getLatitude());
                    if (isInterventionOngoing ) {
                        if (!isAddressOk) {
                            adress = getFineAdress(Objects.requireNonNull(geocoder(location.getLongitude(), location.getLatitude())));
                            intervention.setAddress(adress);
                            rdvViewModel.updateRdv(intervention);
                        }
                    }

                }
            }
        };
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isInterventionOngoing)
            closeIntervention();
        fusedLocationClient.removeLocationUpdates(locationCallback);
        unregisterReceiver(mBroadcastReceiver1);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
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
    private void startForegroundService(){
        EventBus.getDefault().register(this);
        // Create the Foreground Service
        Notification notification = startNotification("Localisation activée");

        startForeground(NOTIFICATION_ID, notification);
        EventBus.getDefault().postSticky(new ForegroundLocationStateEvent(true));
    }
    private void stopForegroundService(){
        Log.d(TAG, "stopForegroundService");
        EventBus.getDefault().unregister(this);
        // Stop foreground service and remove the notification.
        EventBus.getDefault().postSticky(new ForegroundLocationStateEvent(false));
        stopForeground(true);
        // Stop the foreground service.
        stopSelf();
    }
    private void createNotificationChannel(){

            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);

    }
    private void updateNotification(long elpsedTime){
        int minutes = (int) ((elpsedTime / (1000*60)) % 60);
        int hours   = (int) (elpsedTime / (1000*60*60)) ;

        @SuppressLint("DefaultLocale")
        String elapsedTime = String.format("%01d:%02d", hours, minutes);
        Log.d(TAG, "updateNotification: "+elapsedTime);
        Notification notification = startNotification("Temps écoulé "+elapsedTime);

        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.notify(NOTIFICATION_ID,notification);
    }
    private Notification startNotification(String textNotification){
        createNotificationChannel();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        notification =
                new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setCategory(NotificationCompat.CATEGORY_SERVICE)
                        .setContentText(textNotification)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setContentIntent(pendingIntent)
                        .build();
        return notification;
    }
    private void notificationHandler(){
        notificationCode = new Runnable() {
            @Override
            public void run() {
                updateNotification(System.currentTimeMillis()-intervention.getArrival());
                notifHandler.postDelayed(this, 60*1000);
            }
        };
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
            return addresses.get(0).getAddressLine(0);
        } catch (IOException ioException) {
            Log.e(TAG, "Service Not Available", ioException);
            return "Long :"+longitude+","+"Lat :"+latitude;
        } catch (IllegalArgumentException illegalArgumentException) {
            Log.e(TAG, "Invalid Latitude or Longitude Used" + ". " +
                    "Latitude = " + latitude + ", Longitude = " +
                    longitude, illegalArgumentException);
        }
        return NO_ADRESS;
    }
    private String getFineAdress(String adress){

        if (intervention.getAddress().equals(NO_ADRESS)){
            listOfAdress = new HashMap<>();
        }
        Integer val = listOfAdress.get(adress);
        if (val != null){
            listOfAdress.put(adress,++val);
            if (val >= NB_SAME_ADDRESS_IS_OK)
                isAddressOk = true;
        }else{listOfAdress.put(adress,1);}

        Iterator<Map.Entry<String, Integer>> iterator = listOfAdress.entrySet().iterator();
        int valMax = 0;
        while (iterator.hasNext()){
            Map.Entry<String, Integer> mapentry = iterator.next();

            if (mapentry.getValue()>valMax){
                adress = mapentry.getKey();
                valMax = mapentry.getValue();
            }
        }
        return adress;
    }
    private void createNewIntervention(){
        if (!isInterventionOngoing) {
            adress = NO_ADRESS;
            isAddressOk = false;
            if (lastRdv==null || (System.currentTimeMillis() - (lastRdv.getArrival() + lastRdv.getElapsedTime() * 1000)) > MIN_STOP_TIME) {
                    intervention = new Rdv(System.currentTimeMillis());
                    intervention.setAddress(adress);
                    rdvViewModel.insertRdv(intervention);
            } else {
                intervention = new Rdv(lastRdv.getArrival());
                intervention.setAddress(adress);
                intervention.setElapsedTime(0);
                rdvViewModel.updateRdv(intervention);
            }

            EventBus.getDefault().postSticky(new InterventionStateEvent(true));
            isInterventionOngoing = true;
            notifHandler.post(notificationCode);
        }
    }
    private void closeIntervention(){

        if (intervention != null && isInterventionOngoing) {
            long elapsedTime = (System.currentTimeMillis() - intervention.getArrival()) / 1000;
            if (elapsedTime>GPS_REFRESH_INTERVAL*60) {
                intervention.setElapsedTime(elapsedTime);
                rdvViewModel.updateRdv(intervention);
            }else
                rdvViewModel.deleteRdv(intervention);

            EventBus.getDefault().postSticky(new InterventionStateEvent(false));
            isInterventionOngoing = false;
            notifHandler.removeCallbacks(notificationCode);
            updateNotification(0);
        }
    }
    private void loadSettings(){
        SharedPreferences sharedPreferences= getSharedPreferences("pulsatSettings", Context.MODE_PRIVATE);

        if(sharedPreferences!= null) {
            btDevice = sharedPreferences.getString("selectedBtDevice", "");
        } else {
            Toast.makeText(this,"Use the default Domo setting",Toast.LENGTH_LONG).show();
        }
        Log.d(TAG, "loadSettings: btDevice"+btDevice);
    }
    private String getBtDeviceMac(String btDevice){
        if (btDevice != null){
            String [] device =  btDevice.split("\n");
            return device[1];
        }
        return "";
    }
    @Subscribe
    @SuppressWarnings("unused")
    public void onStartInterventionEvent(StartInterventionEvent event){
        createNewIntervention();
    }
    @Subscribe
    @SuppressWarnings("unused")
    public void onStopInterventionEvent(StopInterventionEvent event){
        closeIntervention();
    }
    @Subscribe
    @SuppressWarnings("unused")
    public void onReloadSettingsEvent(ReloadSettingsEvent event){
        loadSettings();
    }
}
