package me.luvletter.planechess.eventargs;

import me.luvletter.planechess.server.PlayerColor;

public class Show_Other_Dice_EventArg {
    public final PlayerColor playerColor;
    public final int result;

    public Show_Other_Dice_EventArg(PlayerColor playerColor, int result) {
        this.playerColor = playerColor;
        this.result = result;
    }
}
