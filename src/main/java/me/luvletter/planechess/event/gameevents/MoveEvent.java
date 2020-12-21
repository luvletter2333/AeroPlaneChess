package me.luvletter.planechess.event.gameevents;

import me.luvletter.planechess.event.Event;
import me.luvletter.planechess.event.EventType;

public class MoveEvent extends Event {
    public final int plane_id;
    public final int step;
    public final boolean go_stack;

    public MoveEvent(int plane_id, int step, boolean go_stack) {
        super(EventType.GameMove);
        this.plane_id = plane_id;
        this.step = step;
        this.go_stack = go_stack;
    }

    @Override
    public String toString() {
        return "MoveEvent{" +
                "plane_id=" + plane_id +
                ", step=" + step +
                ", go_stack=" + go_stack +
                '}';
    }
}
