package com.example.pulsat.event;

import com.example.pulsat.model.Rdv;

public class RdvActionEvent {
    Rdv rdv;
    String action;

    public RdvActionEvent(Rdv rdv, String action){

        this.rdv = rdv;
        this.action = action;
    }

    public Rdv getRdv() {
        return rdv;
    }
    public String getAction() {
        return action;
    }
}
