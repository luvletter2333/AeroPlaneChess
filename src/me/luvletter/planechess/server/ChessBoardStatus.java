package me.luvletter.planechess.server;

import java.util.*;

public class ChessBoardStatus {
    private final int player_Count;
    private LinkedHashMap<Integer, Integer> planePosition;
    private HashMap<Integer, Integer> eatInfo;
    private ArrayList<PlaneStack> stacks;

    public ChessBoardStatus(int player_count, LinkedHashMap<Integer, Integer> pos, HashMap<Integer, Integer> eatInfo, ArrayList<PlaneStack> stacks) {
        this.planePosition = pos;
        this.player_Count = player_count;
        this.stacks = stacks;
        this.eatInfo = eatInfo;
    }

    public int getPlayer_Count() {
        return player_Count;
    }

    /**
     * Key -> Plane ID
     * Value -> Position
     */
    public LinkedHashMap<Integer, Integer> getPlanePosition() {
        return planePosition;
    }

    /**
     * Key -> who eat this plane
     * Value -> plane's ID, which is eaten
     * */
    public HashMap<Integer, Integer> getEatInfo() {
        return eatInfo;
    }

    /**
     * @return a set of PlaneStack
     *
     * */
    public ArrayList<PlaneStack> getStacks() {
        return stacks;
    }


}
