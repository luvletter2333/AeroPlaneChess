package me.luvletter.planechess.event.clientevents;

import me.luvletter.planechess.event.Event;
import me.luvletter.planechess.event.EventType;
import me.luvletter.planechess.server.DiceType;

public final class DiceEvent extends Event {
    public final DiceType diceType;
    public final int diceCount;
    public DiceEvent(DiceType diceType, int diceCount) {
        super(EventType.Dice);
        this.diceType = diceType;
        this.diceCount = diceCount;
    }
}
