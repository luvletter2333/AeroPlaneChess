package me.luvletter.planechess.event.clientevents;

import me.luvletter.planechess.event.Event;
import me.luvletter.planechess.event.EventType;

public class DiceAnimationEvent extends Event {
    public final int result;
    public final int round;
    public final int count;

    public DiceAnimationEvent(int result, int round, int count) {
        super(EventType.DiceAnimation);
        this.result = result;
        this.round = round;
        this.count = count;
    }

    @Override
    public String toString() {
        return "DiceAnimationEvent{" +
                "result=" + result +
                ", round=" + round +
                ", count=" + count +
                '}';
    }
}
