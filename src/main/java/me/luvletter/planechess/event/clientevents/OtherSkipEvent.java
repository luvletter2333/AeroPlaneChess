package me.luvletter.planechess.event.clientevents;

import me.luvletter.planechess.event.Event;
import me.luvletter.planechess.event.EventType;

public class OtherSkipEvent extends Event {
    public final int playerID;

    public OtherSkipEvent(int playerID) {
        super(EventType.OtherSkipEvent);
        this.playerID = playerID;
    }

    @Override
    public String toString() {
        return "OtherSkipEvent{" +
                "playerID=" + playerID +
                '}';
    }
}
