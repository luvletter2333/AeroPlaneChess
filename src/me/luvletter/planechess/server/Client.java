package me.luvletter.planechess.server;


public abstract class Client {

    public final int player_id;
    public Client(int player_id){
        this.player_id = player_id;
    }

    protected abstract void UpdateClientChessBoard(ChessBoardStatus cbs, ServerMovement movement);

    protected abstract void Dice(DiceType diceType, int dice_count, int dice_result);

    protected abstract void ShowOtherDiceResult(int player_id, int dice_result);

    protected abstract void AnnounceWin(int winner);
}
