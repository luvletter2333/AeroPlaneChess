package me.luvletter.planechess.model;

import java.util.*;

public class ChessBoardStatus {
    private int player_Count;
    private HashMap<Integer, Integer> planePosition;
    private ArrayList<PlaneStack> stacks;
    private boolean isWin;
    private int wonPlayer;

    public ChessBoardStatus(int player_count, Map<Integer, Integer> pos, List<PlaneStack> stacks, boolean isWin, int wonPlayer) {
        this.planePosition = new HashMap<>(pos);
        this.player_Count = player_count;
        this.stacks = new ArrayList<>();
        for (PlaneStack stack : stacks)
            this.stacks.add(stack.deepCopy());
        this.isWin = isWin;
        this.wonPlayer = wonPlayer;
    }

    public ChessBoardStatus() {
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

    public void setPlayer_Count(int player_Count) {
        this.player_Count = player_Count;
    }

    public void setPlanePosition(HashMap<Integer, Integer> planePosition) {
        this.planePosition = planePosition;
    }

    public void setStacks(ArrayList<PlaneStack> stacks) {
        this.stacks = stacks;
    }

    public boolean isWin() {
        return isWin;
    }

    public void setWin(boolean win) {
        isWin = win;
    }

    public int getWonPlayer() {
        return wonPlayer;
    }

    public void setWonPlayer(int wonPlayer) {
        this.wonPlayer = wonPlayer;
    }
}
