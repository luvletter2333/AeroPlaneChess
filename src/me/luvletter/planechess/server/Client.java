package me.luvletter.planechess.server;


public abstract class Client {

    public final int player_id;
    private Game game;

    public Client(int player_id) {
        this.player_id = player_id;
    }

    public void bindGame(Game game) {
        this.game = game;
    }

    public abstract boolean isReady();

    public abstract void declareWin(int wonPlayer);

    public abstract void UpdateClientChessBoard(ChessBoardStatus cbs, ServerMovement movement);

    public abstract void Dice(DiceType diceType, int dice_count, int dice_result);

    public abstract void ShowOtherDiceResult(int player_id, DiceType diceType, int dice_count, int dice_result);

    public abstract void AnnounceWin(int winner);
}
