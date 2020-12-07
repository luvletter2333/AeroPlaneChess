package me.luvletter.planechess.server;

import me.luvletter.planechess.event.EventManager;
import me.luvletter.planechess.event.Show_Other_Dice_EventArg;

import java.util.function.Function;

public abstract class Game {

    public final int Player_Count;

    public Game(int player_Count){
        this.Player_Count = player_Count;
    }

    // Rolling Dice, return an integer number in range [1,60]
    // result / 10 -> first. result % 10 -> second
    public abstract int rolling_Dice();


    // Move a plane with given id `step` steps
    // Please check whether data are valid in server side
    public abstract void move(int plane_id, int step);


    // Request From Client
    public abstract ChessBoardStatus getChessboard();


    // Send By Server, Update Client
    protected abstract void UpdateChessBoard();

    protected abstract void AllowDice();

    protected abstract void ShowOtherDiceResult();

}
