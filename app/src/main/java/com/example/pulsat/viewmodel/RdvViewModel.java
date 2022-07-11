package com.example.pulsat.viewmodel;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.pulsat.model.DayRdv;
import com.example.pulsat.model.Rdv;
import com.example.pulsat.repository.RdvRepository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RdvViewModel extends AndroidViewModel {
    final static long MILLISECONDS_IN_A_DAY = 86400000;
    final static String TAG = "RDVVIEWMODEL";
    private RdvRepository mRdvRepository;
    private LiveData<List<Rdv>> mListRdvs;
    private MutableLiveData<List<LiveData<List<Rdv>>>> mListRdvByDate;
    private Rdv rdv;
    public RdvViewModel (Application application) {
        super(application);

        if (this.mRdvRepository != null) {
            return;
        }

        mRdvRepository = new RdvRepository(application);
    }

    public LiveData<List<Rdv>> getMutableAllRdvs(){
        mListRdvs = mRdvRepository.getAllDates();
        return  mListRdvs;
    }

    public LiveData<List<Rdv>> getLiveAdressRdvs(String adress){
        mListRdvs = mRdvRepository.getAdressRdv(adress);
        return  mListRdvs;
    }

    public LiveData<Rdv> getCurrentRdv(){
        return mRdvRepository.getCurrentRdv();
    }
    public List<Rdv> getRdvBeforeDate(Long timestampDate) {return mRdvRepository.getRdvBeforeDate(timestampDate); }

    public void insertRdv(Rdv rdv) {
        mRdvRepository.insertRdv(rdv);
    }
    public void updateRdv(Rdv rdv) {
        mRdvRepository.updateRdv(rdv);
    }
    public void deleteRdv(Rdv rdv) {
        mRdvRepository.deleteRdv(rdv);
    }
    public void deleteAdressRdv(String adress){ mRdvRepository.deleteAdressRdv(adress); };
    public void deleteDayRdv(String date) {
        mRdvRepository.deleteDayRdv(date);
    }
    public void deleteAll() {
        mRdvRepository.deleteAll();
    }
    public void deleteOldestRdvs(){
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        String st = formatter.format(System.currentTimeMillis());
        Date date = null;
        try {
            date = (Date)formatter.parse(st);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int rdvLifetime = getRdvLifetime();
        if (rdvLifetime>0) {
            mRdvRepository.deleteRdvBeforeDate(date.getTime() - rdvLifetime * MILLISECONDS_IN_A_DAY);
        }
    }
    private int getRdvLifetime(){
        SharedPreferences sharedPreferences = getApplication().getSharedPreferences("pulsatSettings", Context.MODE_PRIVATE);

        if(sharedPreferences!= null) {
            return sharedPreferences.getInt("selectedLifetime", 0);
        } else {
            return 0;
        }
    }
}
