package me.luvletter.planechess;

import java.util.Random;

public class InternalServer implements IServer {
    @Override
    public int rolling_Dice() {
        return new Random().nextInt(6) + 1;
    }
}
