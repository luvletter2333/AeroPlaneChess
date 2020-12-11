package me.luvletter.planechess.event.clientevents;

import me.luvletter.planechess.event.Event;
import me.luvletter.planechess.event.EventType;
import me.luvletter.planechess.server.ChessBoardStatus;
import me.luvletter.planechess.server.ServerMovement;

public final class UpdateChessboardEvent extends Event {

    public final ChessBoardStatus cbs;
    public final ServerMovement movement;
    public UpdateChessboardEvent(ChessBoardStatus cbs, ServerMovement movement) {
        super(EventType.UpdateChessboard);
        this.cbs = cbs;
        this.movement = movement;
    }
}
