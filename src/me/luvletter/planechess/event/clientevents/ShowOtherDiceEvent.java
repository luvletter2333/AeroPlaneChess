package me.luvletter.planechess.event.clientevents;

import me.luvletter.planechess.event.Event;
import me.luvletter.planechess.event.EventType;
import me.luvletter.planechess.server.DiceType;

public final class ShowOtherDiceEvent extends Event {
    public final int playerID;
    public final DiceType diceType;
    public final int diceCount;
    public final int diceResult;

    public ShowOtherDiceEvent(int playerID, DiceType diceType, int diceCount, int diceResult) {
        super(EventType.ShowOtherDice);
        this.playerID = playerID;
        this.diceType = diceType;
        this.diceCount = diceCount;
        this.diceResult = diceResult;
    }

    @Override
    public String toString() {
        return "ShowOtherDiceEvent{" +
                "playerID=" + playerID +
                ", diceType=" + diceType +
                ", diceCount=" + diceCount +
                ", diceResult=" + diceResult +
                '}';
    }
}
