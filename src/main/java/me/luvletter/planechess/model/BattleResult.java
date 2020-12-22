package me.luvletter.planechess.model;

public class BattleResult {
    public final int planeID1;
    public final int planeID2;
    public final int dice1;
    public final int dice2;

    public BattleResult(int planeID1, int planeID2, int dice1, int dice2) {
        this.planeID1 = planeID1;
        this.planeID2 = planeID2;
        this.dice1 = dice1;
        this.dice2 = dice2;
    }

    BattleResult(BattleResult oldResult) {
        this.planeID1 = oldResult.planeID1;
        this.planeID2 = oldResult.planeID2;
        this.dice1 = oldResult.dice1;
        this.dice2 = oldResult.dice2;
    }

    public int getWinnerID() {
        return dice1 > dice2 ? this.planeID1 / 10 : this.planeID2 / 10;
    }

    public int getWinnerPlaneID() {
        return dice1 > dice2 ? this.planeID1 : this.planeID2;
    }

    @Override
    public String toString() {
        return "BattleResult{" +
                "Winner=" + getWinnerID() +
                ", planeID1=" + planeID1 +
                ", planeID2=" + planeID2 +
                ", dice1=" + dice1 +
                ", dice2=" + dice2 +
                '}';
    }
}
