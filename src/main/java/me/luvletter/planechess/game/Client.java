package me.luvletter.planechess.game;


import java.util.HashSet;

public abstract class Client {

    public final int player_id;
    protected Game game;

    public Client(int player_id) {
        this.player_id = player_id;
    }

    public void bindGame(Game game) {
        this.game = game;
    }

    public abstract boolean isReady();

    public abstract void UpdateClientChessBoard(ChessBoardStatus cbs, Movement movement, HashSet<Integer> backPlanes, boolean isSkipped, boolean isInitialize);

    public abstract void Dice(DiceType diceType, int dice_count, int dice_result);

    public abstract void ShowOtherDiceResult(int player_id, DiceType diceType, int dice_count, int dice_result);

    public abstract void AnnounceWin(int winner);

    public abstract void AnnounceOtherSkip(int playerID);

    public void takeOff() {
        this.game.takeOff(this.player_id);
    }

    public void move(int planeID, int step, boolean goStack) {
        this.game.move(planeID, step, goStack);
    }

    public void skip() {
        this.game.skip(this.player_id);
    }

    public abstract void AnnounceBattleResult(ChessBoardStatus cbs, Battle battle);
}
