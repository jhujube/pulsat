package com.example.pulsat.model;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

@Entity
public class Rdv implements Serializable {
    @PrimaryKey
    private long mArrival;
    private long mElapsedTime;
    private String mAddress;
    private String mDate;

    public Rdv(long arrival) {
        this.mArrival = arrival;
        this.mElapsedTime = 0;
        @SuppressLint("SimpleDateFormat")
        //SimpleDateFormat formatter = new SimpleDateFormat("d/M/y");
        SimpleDateFormat formatter = new SimpleDateFormat("EEE d MMM");
        this.mDate = formatter.format(new Date(mArrival));

    }

    public long getArrival() {
        return this.mArrival;
    }

    public void setArrival(long mArrival) {
        this.mArrival = mArrival;
    }

    public long getElapsedTime() {
        return this.mElapsedTime;
    }

    public void setElapsedTime(long mElapsedTime) {
        this.mElapsedTime = mElapsedTime;
    }

    public String getStreet() {
        if (mAddress != null) {
            String[] address = this.mAddress.split(",");
            if (address[0] != null) {
                return address[0];
            }
        }
        return"";
    }
    public String getTown() {

        if (mAddress != null) {
            String[] address = this.mAddress.split(",");
            if (address[1] != null) {
                return address[1];
            }
        }
        return "";
    }

    public void setAddress(String mAddress) {this.mAddress = mAddress;}

    public String getAddress() {return this.mAddress;}

    public String getDate() {return this.mDate;}

    public void setDate(String mDate) {this.mDate = mDate;}

}
