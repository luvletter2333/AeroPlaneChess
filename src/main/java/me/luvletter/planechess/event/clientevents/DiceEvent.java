package me.luvletter.planechess.event.clientevents;

import me.luvletter.planechess.event.Event;
import me.luvletter.planechess.event.EventType;
import me.luvletter.planechess.game.DiceType;

public final class DiceEvent extends Event {
    public final DiceType diceType;
    public final int diceCount;
    public final int diceResult;
    public DiceEvent(DiceType diceType, int diceCount, int diceResult) {
        super(EventType.showMyDice);
        this.diceType = diceType;
        this.diceCount = diceCount;
        this.diceResult = diceResult;
    }

    @Override
    public String toString() {
        return "DiceEvent{" +
                "diceType=" + diceType +
                ", diceCount=" + diceCount +
                ", diceResult=" + diceResult +
                '}';
    }
}
