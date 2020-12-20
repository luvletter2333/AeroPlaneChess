package me.luvletter.planechess.event.gameevents;

import me.luvletter.planechess.event.Event;
import me.luvletter.planechess.event.EventType;

public class BattleEvent extends Event {
    public final int planeID;
    public final int step;

    public BattleEvent(int planeID, int step) {
        super(EventType.GameBattle);
        this.planeID = planeID;
        this.step = step;
    }

    @Override
    public String toString() {
        return "BattleEvent{" +
                "planeID=" + planeID +
                ", step=" + step +
                '}';
    }
}
