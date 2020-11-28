package me.luvletter.planechess.server;

public class Player {
    private final PlayerColor color;

    public Player(PlayerColor color){
        this.color = color;
    }

    public PlayerColor getColor() {
        return color;
    }
}
