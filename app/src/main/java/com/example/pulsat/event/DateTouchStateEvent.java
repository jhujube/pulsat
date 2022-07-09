package com.example.pulsat.event;

public class DateTouchStateEvent {
    boolean state;
    public DateTouchStateEvent(Boolean state){
        this.state=state;
    }
    public Boolean getState() {
        return state;
    }
}
