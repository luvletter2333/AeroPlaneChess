package me.luvletter.planechess.server;

import java.util.Hashtable;
import java.util.LinkedHashMap;

public class ChessBoardStatus {
    private final int player_Count;
    private LinkedHashMap<Integer, Integer> position;

    public ChessBoardStatus(int player_count, LinkedHashMap<Integer, Integer> pos) {
        this.position = pos;
        this.player_Count = player_count;
    }

    public int getPlayer_Count() {
        return player_Count;
    }

    /* Key -> Plane ID
     * Value -> Position
     */
    public LinkedHashMap<Integer, Integer> getPosition() {
        return position;
    }
}
