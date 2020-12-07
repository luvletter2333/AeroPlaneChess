package me.luvletter.planechess.server;

import java.util.*;

public class InternalGame extends Game {

    public InternalGame(int player_Count) {
        super(player_Count);
    }

    @Override
    public int rolling_Dice() {
        var random =  new Random();
        int first = random.nextInt(6) + 1;
        int second = random.nextInt(6) + 1;
        return first * 10 + second;
  //      return new Random().nextInt(6) + 1;
    }

    @Override
    public void move(int plane_id, int step) {

    }

    @Override
    public ChessBoardStatus getChessboard() {
        var cb = new ChessBoardStatus(4,new LinkedHashMap<>(){{
            put(11,101);
            put(12,100);
            put(13,100);
            put(14,100);
            put(21,205);
            put(22,200);
            put(23,200);
            put(24,200);
            put(31,307);
            put(32,300);
            put(33,300);
            put(34,300);
            put(41,400);
            put(42,400);
            put(43,418);
            put(44,400);
        }},new HashMap<>(), new ArrayList<>());
        return cb;
    }

    public void Allow_Dice(){
        if(runnable_allow_dice != null)
            runnable_allow_dice.run();
    }

    public void forceupdate() {
        runnable_update_chessboard.apply(getChessboard());
    }
}
