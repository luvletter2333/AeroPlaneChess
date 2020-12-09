package me.luvletter.planechess.server;

import java.util.*;

public abstract class Game {

    public final int Player_Count;
    protected final ArrayList<Integer> player_ids;
    protected final HashMap<Integer, Integer> planePosition;
    protected final ArrayList<PlaneStack> planeStacks;

    // Dice related:
    protected int dice_player_id;
    protected int dice_first_result;
    protected int dice_second_result;
    protected boolean dice_moved = false;
    protected Random dice_random;


    public Game(int player_Count, ArrayList<Integer> player_ids){
        this.Player_Count = player_Count;
        if(player_ids.size() != player_Count)
            throw new IllegalArgumentException("size of player_ids should equal to player_Count");
        this.player_ids = new ArrayList<>(player_ids);
        this.planePosition = new HashMap<>();
        initializePlanePosition();
        this.planeStacks = new ArrayList<>();
        this.dice_random = new Random();
    }

    private void initializePlanePosition(){
        //TODO: initialize planePosition;
    }

    /**
     * Rolling Dice, return an integer number in range [1,60]
     * result / 10 -> first. result % 10 -> second
     * */
    public abstract int rolling_Dice(int plane_id);


    /**
     * Move a plane with given id `step` steps
     * Please check whether data are valid in server side
     *
     * @return*/
    public abstract boolean move(int plane_id, int step);

    /**
     * Request From Client
     * */
    public abstract ChessBoardStatus getChessboardStatus();

    /**
     * Send By Server, Update Client
     * */
    protected abstract void UpdateClientChessBoard();

    protected abstract void AllowDice();

    protected abstract void ShowOtherDiceResult();

}
