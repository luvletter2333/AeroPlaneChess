package me.luvletter.planechess.server;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;

public class ChessBoardStatus {
    private final int player_Count;
    private LinkedHashMap<Integer, Integer> position;
    private HashMap<Integer, Integer> eatInfo;

    public ChessBoardStatus(int player_count, LinkedHashMap<Integer, Integer> pos, HashMap<Integer, Integer> eatInfo) {
        this.position = pos;
        this.player_Count = player_count;
        this.eatInfo = eatInfo;
    }

    public int getPlayer_Count() {
        return player_Count;
    }

    /**
     * Key -> Plane ID
     * Value -> Position
     */
    public LinkedHashMap<Integer, Integer> getPosition() {
        return position;
    }

    /**
     * Key -> who eat this plane
     * Value -> plane's ID, which is eaten
     * */
    public HashMap<Integer, Integer> getEatInfo() {
        return eatInfo;
    }
}
