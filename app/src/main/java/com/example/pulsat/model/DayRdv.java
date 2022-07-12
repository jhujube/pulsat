package com.example.pulsat.model;

import androidx.lifecycle.LiveData;

import java.util.List;

public class DayRdv {
    private String day;
    private List<Rdv> rdvList;

    public  DayRdv(){};
    public DayRdv(String day, List<Rdv> rdvList){
        this.day = day;
        this.rdvList = rdvList;
    }
    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public List<Rdv> getRdvList() {
        return rdvList;
    }

    public Rdv getRdvAtPos (int pos){ return rdvList.get(pos);}

    public Rdv removeRdvAtPos (int pos){ return rdvList.remove(pos);}

    public int rdvListSize () {return rdvList.size();}

    public void add(int position, Rdv rdv) {rdvList.add(position,rdv);}

    public void setRdvList(List<Rdv> rdvList) {
        this.rdvList = rdvList;
    }

    public String info() {
        return ("Taille de DayRdv :"+rdvList.size());
    }

}
