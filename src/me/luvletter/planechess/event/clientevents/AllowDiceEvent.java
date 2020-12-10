package me.luvletter.planechess.event.clientevents;

import me.luvletter.planechess.event.Event;
import me.luvletter.planechess.event.EventType;

public final class AllowDiceEvent extends Event {
    public AllowDiceEvent() {
        super(EventType.AllowDice);
    }
}
