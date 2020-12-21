package me.luvletter.planechess.game;

/**
 * Fly -> True
 * Battle -> False
 * */
public enum DiceType {
    Fly,
    Battle;

    public boolean getIntValue(){
        return this.equals(Fly);
    }

    public static DiceType getDiceType(boolean isFly){
        return isFly ? Fly : Battle;
    }
}
