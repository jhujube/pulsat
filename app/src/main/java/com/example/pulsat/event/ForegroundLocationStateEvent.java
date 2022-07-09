package com.example.pulsat.event;

public class ForegroundLocationStateEvent {
    Boolean state;

    public ForegroundLocationStateEvent(Boolean state){
        this.state=state;
    }
    public Boolean getState() {
        return state;
    }
}
