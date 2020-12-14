package me.luvletter.planechess.client;

public class Point {
    public final int X;
    public final int Y;

    public Point(int x, int y) {
        X = x;
        Y = y;
    }

    public Point(double x, double y) {
        X = (int) x;
        Y = (int) y;
    }

    @Override
    public String toString() {
        return "Point " + X + " " + Y;
    }
}