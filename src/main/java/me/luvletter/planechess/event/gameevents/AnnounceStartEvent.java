package me.luvletter.planechess.event.gameevents;

import me.luvletter.planechess.event.Event;
import me.luvletter.planechess.event.EventType;

public class AnnounceStartEvent extends Event {
    public AnnounceStartEvent(){
        super(EventType.GameAnnounceStart);
    }

    @Override
    public String toString() {
        return "AnnounceStartEvent{}";
    }
}
