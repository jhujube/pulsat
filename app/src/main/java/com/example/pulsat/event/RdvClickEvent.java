package com.example.pulsat.event;

import com.example.pulsat.model.Rdv;

public class RdvClickEvent {
    Rdv rdv;

    public Rdv getRdv() {
        return rdv;
    }

    public RdvClickEvent(Rdv rdv){
        this.rdv = rdv;
    }
}
