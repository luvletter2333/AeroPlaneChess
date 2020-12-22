package me.luvletter.planechess.model;

import java.util.ArrayList;

public class Movement {
    public int planeID;
    public int startPos;
    public int endPos;
    public ArrayList<Integer> keypoint;

    public Movement(int planeID, int startPos, int endPos) {
        this.planeID = planeID;
        this.startPos = startPos;
        this.endPos = endPos;
        this.keypoint = new ArrayList<>();
    }

    public Movement() {
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

    public int getPlaneID() {
        return planeID;
    }

    public void setPlaneID(int planeID) {
        this.planeID = planeID;
    }

    public int getStartPos() {
        return startPos;
    }

    public void setStartPos(int startPos) {
        this.startPos = startPos;
    }

    public int getEndPos() {
        return endPos;
    }

    public void setEndPos(int endPos) {
        this.endPos = endPos;
    }

    public ArrayList<Integer> getKeypoint() {
        return keypoint;
    }

    public void setKeypoint(ArrayList<Integer> keypoint) {
        this.keypoint = keypoint;
    }
}
