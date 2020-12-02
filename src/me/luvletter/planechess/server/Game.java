package me.luvletter.planechess.server;

import me.luvletter.planechess.eventargs.Show_Other_Dice_EventArg;

import javax.print.attribute.standard.JobKOctets;
import java.util.concurrent.Callable;
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

    public abstract ChessBoardStatus getChessboard();

    protected Function<ChessBoardStatus, Object> runnable_update_chessboard;
    public void addCallback_update_chessboard(Function<ChessBoardStatus, Object> _update_chessboard){
        this.runnable_update_chessboard = _update_chessboard;
    }

    protected Runnable runnable_allow_dice;
    public void addCallback_Allow_Dice(Runnable _allow_dice){
        this.runnable_allow_dice = _allow_dice;
    }

    protected Function<Show_Other_Dice_EventArg, Object> runnable_show_other_dice;
    public void addCallback_Show_Other_Dice(Function<Show_Other_Dice_EventArg, Object> _runnable_show_other_dice){
        this.runnable_show_other_dice = _runnable_show_other_dice;
    }

}
