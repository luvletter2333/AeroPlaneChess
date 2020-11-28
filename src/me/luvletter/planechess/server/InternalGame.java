package me.luvletter.planechess.server;

import java.util.Random;

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
    public ChessBoardStatus move(int plane_id, int step) {
        return null;
    }

    @Override
    public ChessBoardStatus getChessboard() {
        return null;
    }

    public void Allow_Dice(){
        if(runnable_allow_dice != null)
            runnable_allow_dice.run();
    }
}
