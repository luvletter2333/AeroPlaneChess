package me.luvletter.planechess.event.gameevents;

import me.luvletter.planechess.event.Event;
import me.luvletter.planechess.event.EventType;

public class TakeOffEvent extends Event {
    public final int playerID;

    public TakeOffEvent(int playerID) {
        super(EventType.GameTakeOff);
        this.playerID = playerID;
    }

    @Override
    public String toString() {
        return "TakeOffEvent{" +
                "playerID=" + playerID +
                '}';
    }
}
