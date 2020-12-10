package me.luvletter.planechess.event.clientevents;

import me.luvletter.planechess.event.Event;
import me.luvletter.planechess.event.EventType;

public class AnnounceWinEvent extends Event {
    public final int winner_id;
    public AnnounceWinEvent(int winner_id){
        super(EventType.AnnounceWin);
        this.winner_id = winner_id;
    }
}
