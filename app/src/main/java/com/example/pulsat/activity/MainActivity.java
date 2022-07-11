package com.example.pulsat.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.lights.LightState;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pulsat.R;
import com.example.pulsat.adapter.CustomAdapterHorizontal;
import com.example.pulsat.event.DateTouchStateEvent;
import com.example.pulsat.event.ForegroundLocationStateEvent;
import com.example.pulsat.event.InterventionStateEvent;
import com.example.pulsat.event.RdvClickEvent;
import com.example.pulsat.event.RequestAccesFineLocationPermissionEvent;
import com.example.pulsat.event.RequestBluetoothConnectPermissionEvent;
import com.example.pulsat.model.Rdv;
import com.example.pulsat.permission.PermissionManager;
import com.example.pulsat.permission.PermissionStatus;
import com.example.pulsat.viewmodel.LocationViewModel;
import com.example.pulsat.viewmodel.RdvViewModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.security.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;


public class MainActivity extends AppCompatActivity implements LifecycleOwner {

    final static int ACCES_FINE_PERMISSION_REQUEST_CODE = 1; // valeur arbitraire
    final static int BLUETOOTH_CONNECT_PERMISSION_REQUEST_CODE = 2;
    final static String TAG = "MAINACTIVITY";
    private Chronometer chrono;
    private ImageButton buttonChrono;
    private RdvViewModel rdvViewModel;
    private CustomAdapterHorizontal customAdapterHorizontal;
    private CustomLinearLayoutManager layoutManager;
    private LocationViewModel locationViewModel;
    private MainActivity context;
    private Boolean isForegroundLocationRunning = false;
    private Boolean isInterventionOn = false;

    @Override
    protected void onStart(){
        super.onStart();
        EventBus.getDefault().register(this);
        locationViewModel.startLocation(isForegroundLocationRunning);
    }

    @Override
    protected void onStop(){
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        locationViewModel = new LocationViewModel(this);

        chrono = findViewById(R.id.chronometer);

        buttonChrono = findViewById(R.id.buttonChrono);
        buttonChrono.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isInterventionOn)
                    locationViewModel.manualStopIntervention();
                else
                    locationViewModel.manualStartIntervention();
            }
        });

        RecyclerView mRecyclerView = findViewById(R.id.recyclerHorizontal);
        customAdapterHorizontal = new CustomAdapterHorizontal();

        mRecyclerView.setAdapter(customAdapterHorizontal);
        layoutManager = new CustomLinearLayoutManager(this,
                RecyclerView.HORIZONTAL, // direction
                true);
        mRecyclerView.setLayoutManager(layoutManager); // sens)
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addOnScrollListener(new CustomScrollListener());
        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(mRecyclerView);

        // Get a new or existing ViewModel from the ViewModelProvider.
        rdvViewModel = new ViewModelProvider(context).get(RdvViewModel.class);

        rdvViewModel.getMutableAllRdvs().observe(this, listDatesObserver ->{
                customAdapterHorizontal.updateDatesList(listDatesObserver);
                customAdapterHorizontal.notifyDataSetChanged();
        });

        rdvViewModel.getCurrentRdv().observe(this,rdv -> {
            if (rdv != null) {
                if (rdv.getElapsedTime() == 0) {
                    chrono.start();
                    chrono.setBase(SystemClock.elapsedRealtime() - (System.currentTimeMillis() - rdv.getArrival()));
                } else {
                    chrono.stop();
                    chrono.setBase(SystemClock.elapsedRealtime() - rdv.getElapsedTime() * 1000);
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        rdvViewModel.deleteOldestRdvs();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d("MAINACTIVITY", "onRequestPermission..."+requestCode);
        switch (requestCode){

            case ACCES_FINE_PERMISSION_REQUEST_CODE:
                Log.d("MAINACTIVITY", "onRequestPermission ACCES_FINE ");
                if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationViewModel.startLocation(isForegroundLocationRunning);
                }else if (PermissionManager.getPermissionStatus(this, Manifest.permission.ACCESS_FINE_LOCATION)==PermissionStatus.PERMISSION_DENIED){
                    openAppSettingEvent();
                }
                break;
            case BLUETOOTH_CONNECT_PERMISSION_REQUEST_CODE:
                Log.d("MAINACTIVITY", "onRequestPermission ACCES_FINE ");
                if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationViewModel.startLocation(isForegroundLocationRunning);
                }else if (PermissionManager.getPermissionStatus(this, Manifest.permission.BLUETOOTH_CONNECT)==PermissionStatus.PERMISSION_DENIED){
                    openAppSettingEvent();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onRequestAccesFineLocationPermissionEvent(RequestAccesFineLocationPermissionEvent event){
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ACCES_FINE_PERMISSION_REQUEST_CODE);
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onRequestBluetoothConnectPermissionEvent(RequestBluetoothConnectPermissionEvent event){
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.BLUETOOTH_CONNECT}, BLUETOOTH_CONNECT_PERMISSION_REQUEST_CODE);
    }


    public void openAppSettingEvent(){
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.parse("package:"+getApplicationContext().getPackageName());
        intent.setData(uri);
        startActivity(intent);
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onRdvClickEvent(RdvClickEvent event){
        deleteRdv(event.getRdv());
    }

    @Subscribe (sticky = true, threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onForegroundLocationStateEvent(ForegroundLocationStateEvent event){
        isForegroundLocationRunning = event.getState();
    }

    @Subscribe (sticky = true, threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onInterventionStateEvent(InterventionStateEvent event){
        isInterventionOn = event.getState();
        buttonChrono.setImageResource( isInterventionOn ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
    }

    private void deleteRdv(Rdv rdv){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Gerer le rendez-vous")
                .setTitle("Gestion");
        builder.setPositiveButton("Effacer", (dialog, id) -> {
            Log.d("MAINACTIVITY", "delete rdv n°"+rdv.getArrival());
            rdvViewModel.deleteRdv(rdv);
        });
        builder.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });
        builder.setNeutralButton("Regrouper", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent RdvByAdressActivityIntent = new Intent(MainActivity.this, RdvByAdressActivity.class);
                RdvByAdressActivityIntent.putExtra("rdv",rdv);
                startActivity(RdvByAdressActivityIntent);
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuItem_settings:
                Intent settingsActivityIntent = new Intent(context, SettingsActivity.class);
                startActivity(settingsActivityIntent);
                return true;
            case R.id.menuItem_quit:
                locationViewModel.stopForegroundService();
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @Subscribe
    @SuppressWarnings("unused")
    public void onDateTouchStateEvent(DateTouchStateEvent event){
        Log.d(TAG, "onDateTouchStateEvent: "+event.getState());
        layoutManager.setScrollEnabled(event.getState());
    }
    public class CustomLinearLayoutManager extends LinearLayoutManager {
        private boolean isScrollEnabled = false;

        public CustomLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
            super(context, orientation, reverseLayout);
        }

        public void setScrollEnabled(boolean flag) {
            this.isScrollEnabled = flag;
        }

        @Override
        public boolean canScrollHorizontally() {
            //Similarly you can customize "canScrollHorizontally()" for managing horizontal scroll
            return isScrollEnabled && super.canScrollHorizontally();
        }
    }
    public class CustomScrollListener extends RecyclerView.OnScrollListener {
        public CustomScrollListener() {
        }

        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            switch (newState) {
                case RecyclerView.SCROLL_STATE_IDLE:
                    layoutManager.setScrollEnabled(false);
                    break;
            }

        }
    }
}