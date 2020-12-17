package me.luvletter.planechess.event.clientevents;

import me.luvletter.planechess.event.Event;
import me.luvletter.planechess.event.EventType;
import me.luvletter.planechess.server.ChessBoardStatus;
import me.luvletter.planechess.server.Movement;

import java.util.ArrayList;
import java.util.HashSet;

public final class UpdateChessboardEvent extends Event {

    public final ChessBoardStatus cbs;
    public final boolean isSkipped;
    public final boolean isInitialize;
    public final Movement movement;
    public final HashSet<Integer> backPlanes;

    public UpdateChessboardEvent(ChessBoardStatus cbs, Movement movement, HashSet<Integer> backPlanes, boolean isSkipped, boolean isInitialize) {
        super(EventType.UpdateChessboard);
        this.cbs = cbs;
        this.movement = movement;
        this.backPlanes = new HashSet<>();
        if (backPlanes != null)
            this.backPlanes.addAll(backPlanes);
        this.isSkipped = isSkipped;
        this.isInitialize = isInitialize;
    }

    @Override
    public String toString() {
        return "UpdateChessboardEvent{" +
                "cbs=" + cbs +
                ", isSkipped=" + isSkipped +
                ", isInitialize=" + isInitialize +
                ", movement=" + movement +
                ", backPlanes=" + backPlanes +
                '}';
    }
}
