package me.luvletter.planechess.event.clientevents;

import me.luvletter.planechess.event.Event;
import me.luvletter.planechess.event.EventType;
import me.luvletter.planechess.server.ChessBoardStatus;

public final class UpdateChessboardEvent extends Event {

    public final ChessBoardStatus cbs;
    public UpdateChessboardEvent(ChessBoardStatus cbs) {
        super(EventType.UpdateChessboard);
        this.cbs = cbs;
    }
}
