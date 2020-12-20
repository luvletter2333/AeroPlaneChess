package me.luvletter.planechess.event.clientevents;

import me.luvletter.planechess.client.Point;
import me.luvletter.planechess.event.Event;
import me.luvletter.planechess.event.EventType;

public class PreviewEvent extends Event {
    public final Point clickPoint;
    public PreviewEvent(Point clickPoint) {
        super(EventType.Preview);
        this.clickPoint = clickPoint;
    }

    @Override
    public String toString() {
        return "PreviewEvent{" +
                "clickPoint=" + clickPoint +
                '}';
    }
}
