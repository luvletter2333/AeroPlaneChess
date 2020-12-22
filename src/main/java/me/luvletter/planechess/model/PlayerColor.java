package me.luvletter.planechess.model;

public enum PlayerColor {
    Red,
    Yellow,
    Blue,
    Green;

    public int getIntValue() {
        return switch (this) {
            case Red -> 1;
            case Yellow -> 2;
            case Blue -> 3;
            case Green -> 4;
        };
    }

    public static PlayerColor getPlayerColor(int player_id) {
        return switch (player_id) {
            case 1 -> Red;
            case 2 -> Yellow;
            case 3 -> Blue;
            case 4 -> Green;
            default -> null;
        };
    }

    public static String getFriendString(int playerID) {
        return getPlayerColor(playerID).toString();
    }
}
