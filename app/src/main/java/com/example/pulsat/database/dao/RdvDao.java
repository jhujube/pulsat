package com.example.pulsat.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.pulsat.model.Rdv;

import java.util.List;

@Dao
public interface RdvDao {

    @Query("SELECT * FROM Rdv ORDER BY mArrival DESC")
    LiveData<List<Rdv>> getAllRdv();

    @Query("SELECT * FROM Rdv WHERE mDate = :date ORDER BY mArrival DESC")
    LiveData<List<Rdv>> getRdvDate(String date);

    @Query("SELECT * FROM Rdv WHERE mArrival < :timestampDate ORDER BY mArrival DESC")
    List<Rdv> getRdvBeforeDate(Long timestampDate);

    @Query("DELETE FROM Rdv WHERE mArrival < :timestampDate")
    int deleteRdvBeforeDate(Long timestampDate);

    @Query("SELECT * FROM Rdv WHERE mAddress = :Adress ORDER BY mArrival DESC")
    LiveData<List<Rdv>> getAdressRdv(String Adress);

    @Query("SELECT * FROM Rdv ORDER BY mArrival DESC LIMIT 1")
    LiveData<Rdv> getCurrentRdv();

    @Query("SELECT * FROM Rdv WHERE mArrival = :arrival")
    Rdv getRdv(long arrival);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertRdv(Rdv rdv);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    int updateRdv(Rdv rdv);

    @Delete
    int deleteRdv(Rdv rdv);

    @Query("DELETE FROM Rdv WHERE mAddress = :adress")
    int deleteAdressRdv(String adress);

    @Query("DELETE FROM Rdv WHERE mDate = :date")
    int deleteDay(String date);

    @Query("DELETE FROM Rdv ")
    int deleteAll();
}
