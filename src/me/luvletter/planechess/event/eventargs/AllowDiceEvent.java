package me.luvletter.planechess.event.eventargs;

import me.luvletter.planechess.event.Event;
import me.luvletter.planechess.event.EventType;

public final class AllowDiceEvent extends Event {
    public AllowDiceEvent() {
        super(EventType.AllowDice);
    }
}
