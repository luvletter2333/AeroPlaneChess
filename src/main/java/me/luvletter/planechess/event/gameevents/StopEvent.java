package me.luvletter.planechess.event.gameevents;

import me.luvletter.planechess.event.Event;
import me.luvletter.planechess.event.EventType;

public class StopEvent extends Event {
    public StopEvent() {
        super(EventType.GameStop);
    }
}
