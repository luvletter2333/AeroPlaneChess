package me.luvletter.planechess.event.gameevents;

import me.luvletter.planechess.event.Event;
import me.luvletter.planechess.event.EventType;

public class SkipEvent extends Event {
    public final int playerID;

    public SkipEvent(int playerID) {
        super(EventType.GameSkip);
        this.playerID = playerID;
    }

    @Override
    public String toString() {
        return "SkipEvent{" +
                "playerID=" + playerID +
                '}';
    }
}
