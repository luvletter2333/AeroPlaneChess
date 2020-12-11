package me.luvletter.planechess.event.clientevents;

import me.luvletter.planechess.event.Event;
import me.luvletter.planechess.event.EventType;
import me.luvletter.planechess.server.PlayerColor;

public final class ShowOtherDiceEvent extends Event {
    private final int playerColor;
    private final int result;

    public ShowOtherDiceEvent(int playerColor, int result) {
        super(EventType.ShowOtherDiceEvent);
        this.playerColor = playerColor;
        this.result = result;
    }
}
