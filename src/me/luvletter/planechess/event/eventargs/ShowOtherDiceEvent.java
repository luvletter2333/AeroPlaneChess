package me.luvletter.planechess.event.eventargs;

import me.luvletter.planechess.event.Event;
import me.luvletter.planechess.event.EventType;
import me.luvletter.planechess.server.PlayerColor;

public final class ShowOtherDiceEvent extends Event {
    private final PlayerColor playerColor;
    private final int result;

    public ShowOtherDiceEvent(PlayerColor playerColor, int result) {
        super(EventType.ShowOtherDiceEvent);
        this.playerColor = playerColor;
        this.result = result;
    }
}
