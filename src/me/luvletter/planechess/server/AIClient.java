package me.luvletter.planechess.server;

import java.util.HashSet;

public class AIClient extends Client {
    public AIClient(int player_id) {
        super(player_id);
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void declareWin(int wonPlayer) {

    }

    @Override
    public void UpdateClientChessBoard(ChessBoardStatus cbs, Movement movement, HashSet<Integer> backPlanes, boolean isSkippped, boolean isInitialize){

    }

    @Override
    public void Dice(DiceType diceType, int dice_count, int dice_result) {
        System.out.printf("[AI] %s %s %s\n", diceType, dice_count, dice_result);
    }

    @Override
    public void ShowOtherDiceResult(int player_id, DiceType diceType, int dice_count, int dice_result) {

    }

    @Override
    public void AnnounceWin(int winner) {

    }
}
