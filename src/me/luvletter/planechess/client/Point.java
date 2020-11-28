package me.luvletter.planechess.client;

public class Point{
    public final int X;
    public final int Y;

    public Point(int x, int y) {
        X = x;
        Y = y;
    }

    @Override
    public String toString() {
        return "Point "+ X + " " + Y;
    }
}