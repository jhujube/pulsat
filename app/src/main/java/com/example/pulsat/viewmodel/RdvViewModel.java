package com.example.pulsat.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.pulsat.model.DayRdv;
import com.example.pulsat.model.Rdv;
import com.example.pulsat.repository.RdvRepository;

import java.util.ArrayList;
import java.util.List;

public class RdvViewModel extends AndroidViewModel {

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
    public List<Rdv> getRdvBeforeDate(Long timestampDate) {return mRdvRepository.getRdvAfterDate(timestampDate); }

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

}
