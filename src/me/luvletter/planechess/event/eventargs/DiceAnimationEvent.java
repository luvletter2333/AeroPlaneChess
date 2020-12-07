package me.luvletter.planechess.event.eventargs;

import me.luvletter.planechess.event.Event;
import me.luvletter.planechess.event.EventType;

public class DiceAnimationEvent extends Event {
    public final int firstResult;
    public final int secondResult;
    public DiceAnimationEvent(int first_result, int second_result){
        super(EventType.DiceAnimation);
        this.firstResult = first_result;
        this.secondResult = second_result;
    }
}
