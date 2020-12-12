package me.luvletter.planechess.server;

import java.util.*;

public class ChessBoardStatus {
    private final int player_Count;
    private HashMap<Integer, Integer> planePosition;
    private final ArrayList<PlaneStack> stacks;
    private final boolean isWin;
    private final int wonPlayer;

    public ChessBoardStatus(int player_count, HashMap<Integer, Integer> pos, ArrayList<PlaneStack> stacks, boolean isWin, int wonPlayer) {
        this.planePosition = pos;
        this.player_Count = player_count;
        this.stacks = stacks;
        this.isWin = isWin;
        this.wonPlayer = wonPlayer;
    }

    public int getPlayer_Count() {
        return player_Count;
    }

    /**
     * Key -> Plane ID
     * Value -> Position
     */
    public HashMap<Integer, Integer> getPlanePosition() {
        return planePosition;
    }
//
//    /**
//     * Key -> who eat this plane
//     * Value -> plane's ID, which is eaten
//     * */
//    public HashMap<Integer, Integer> getEatInfo() {
//        return eatInfo;
//    }

    /**
     * @return a set of PlaneStack
     *
     * */
    public ArrayList<PlaneStack> getStacks() {
        return stacks;
    }


}
