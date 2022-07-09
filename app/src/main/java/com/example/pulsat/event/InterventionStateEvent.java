package com.example.pulsat.event;

public class InterventionStateEvent {
    boolean state;
    public InterventionStateEvent(Boolean state){
        this.state=state;
    }
    public Boolean getState() {
        return state;
    }
}
