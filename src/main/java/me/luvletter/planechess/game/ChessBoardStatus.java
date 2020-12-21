package me.luvletter.planechess.game;

import java.util.*;

public class ChessBoardStatus {
    private final int player_Count;
    private HashMap<Integer, Integer> planePosition;
    private final ArrayList<PlaneStack> stacks;
    private final boolean isWin;
    private final int wonPlayer;

    public ChessBoardStatus(int player_count, HashMap<Integer, Integer> pos, ArrayList<PlaneStack> stacks, boolean isWin, int wonPlayer) {
        this.planePosition = new HashMap<>(pos);
        this.player_Count = player_count;
        this.stacks = new ArrayList<>();
        for (PlaneStack stack : stacks)
            this.stacks.add(stack.deepCopy());
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
     */
    public ArrayList<PlaneStack> getStacks() {
        return stacks;
    }

    @Override
    public String toString() {
        return "ChessBoardStatus{" +
                "player_Count=" + player_Count +
                ", planePosition=" + planePosition +
                ", stacks=" + stacks +
                ", isWin=" + isWin +
                ", wonPlayer=" + wonPlayer +
                '}';
    }
}