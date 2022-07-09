package com.example.pulsat.repository;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.pulsat.R;
import com.example.pulsat.model.DayRdv;
import com.example.pulsat.model.Rdv;
import com.example.pulsat.database.RdvDatabase;
import com.example.pulsat.database.dao.RdvDao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class RdvRepository {
    private RdvDao mRdvDao;
    private LiveData<List<Rdv>> mAllDates;
    private LiveData<Rdv> mCurrentRdv;
    private LiveData<List<Rdv>> mListRdv;
    private List<Rdv> listRdv;

    public RdvRepository(Application application) {
        RdvDatabase db = RdvDatabase.getDatabase(application);
        mRdvDao = db.rdvDao();
        mAllDates = new MutableLiveData<>();

    }

    public LiveData<List<Rdv>> getDateRdv(String date){
        mListRdv = mRdvDao.getRdvDate(date);
        return mListRdv;
    }

    public List<Rdv> getRdvAfterDate(Long timestampDate){
        listRdv = mRdvDao.getRdvBeforeDate(timestampDate);
        return listRdv;
    }

    public LiveData<List<Rdv>> getAllDates() {
        mListRdv = mRdvDao.getAllRdv();
        return mListRdv;
    }

    public LiveData<List<Rdv>> getAdressRdv(String adress) {
        mListRdv = mRdvDao.getAdressRdv(adress);
        return mListRdv;
    }

    public LiveData<Rdv> getCurrentRdv() {
        mCurrentRdv = mRdvDao.getCurrentRdv();
        if (mCurrentRdv.getValue() != null){
        Log.d("RDVREPOSITORY", ""+mCurrentRdv.getValue().getElapsedTime());
        }
        return mCurrentRdv;
    }

    public void insertRdv(Rdv rdv) {
        RdvDatabase.databaseWriteExecutor.execute(() -> {
            mRdvDao.insertRdv(rdv);
        });
    }

    public void updateRdv(Rdv rdv) {
        RdvDatabase.databaseWriteExecutor.execute(() -> {
            mRdvDao.updateRdv(rdv);
        });
    }
    public void deleteRdv(Rdv rdv) {
        RdvDatabase.databaseWriteExecutor.execute(() -> {
            mRdvDao.deleteRdv(rdv);
        });
    }
    public void deleteAdressRdv(String adress) {
        RdvDatabase.databaseWriteExecutor.execute(() -> {
            mRdvDao.deleteAdressRdv(adress);
        });
    }
    public void deleteDayRdv(String date) {
        RdvDatabase.databaseWriteExecutor.execute(() -> {
            mRdvDao.deleteDay(date);
        });
    }
    public void deleteAll() {
        RdvDatabase.databaseWriteExecutor.execute(() -> {
            mRdvDao.deleteAll();
        });
    }

}

