package me.luvletter.planechess.game;

import me.luvletter.planechess.event.gameevents.BattleEvent;
import me.luvletter.planechess.event.gameevents.MoveEvent;
import me.luvletter.planechess.event.gameevents.SkipEvent;
import me.luvletter.planechess.event.gameevents.TakeOffEvent;

public interface IGame {
    void takeOff(int player_id);

    void skip(int player_id);

    void move(int plane_id, int step, boolean go_stack);

    void battle(int planeID, int step);

}
