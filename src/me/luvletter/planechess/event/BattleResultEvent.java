package me.luvletter.planechess.event;

import me.luvletter.planechess.server.Battle;
import me.luvletter.planechess.server.ChessBoardStatus;

public class BattleResultEvent extends Event {

    public final ChessBoardStatus chessBoardStatus;
    public final Battle Result;

    public BattleResultEvent(ChessBoardStatus chessBoardStatus, Battle result) {
        super(EventType.BattleResult);
        this.chessBoardStatus = chessBoardStatus;
        this.Result = result;
    }
}
