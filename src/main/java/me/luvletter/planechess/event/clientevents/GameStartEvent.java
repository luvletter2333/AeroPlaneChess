package me.luvletter.planechess.event.clientevents;

import me.luvletter.planechess.event.Event;
import me.luvletter.planechess.event.EventType;

public class GameStartEvent extends Event {
    public GameStartEvent() {
        super(EventType.GameStart);
    }
}
