package me.luvletter.planechess.model;

public class Player {
    private final PlayerColor color;

    public Player(PlayerColor color){
        this.color = color;
    }

    public PlayerColor getColor() {
        return color;
    }
}
