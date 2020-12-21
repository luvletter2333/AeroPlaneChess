package me.luvletter.planechess.game;

import java.util.ArrayList;

public class Movement {
    public final int planeID;
    public final int startPos;
    public final int endPos;
    public final ArrayList<Integer> keypoint;

    public Movement(int planeID, int startPos, int endPos) {
        this.planeID = planeID;
        this.startPos = startPos;
        this.endPos = endPos;
        this.keypoint = new ArrayList<>();
    }

    public Movement addKeyPoint(Integer positionID)
    {
        keypoint.add(positionID);
        return this;
    }

    @Override
    public String toString() {
        return "Movement{" +
                "planeID=" + planeID +
                ", startPos=" + startPos +
                ", endPos=" + endPos +
                ", keypoint=" + keypoint +
                '}';
    }
}
