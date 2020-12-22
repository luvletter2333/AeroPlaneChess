package me.luvletter.planechess.event.clientevents;

import me.luvletter.planechess.event.Event;
import me.luvletter.planechess.event.EventType;
import me.luvletter.planechess.model.Battle;
import me.luvletter.planechess.model.ChessBoardStatus;

public class BattleResultEvent extends Event {

    public final ChessBoardStatus chessBoardStatus;
    public final Battle Result;

    public BattleResultEvent(ChessBoardStatus chessBoardStatus, Battle result) {
        super(EventType.BattleResult);
        this.chessBoardStatus = chessBoardStatus;
        this.Result = result;
    }
}
