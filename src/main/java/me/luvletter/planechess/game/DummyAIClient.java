package me.luvletter.planechess.game;

import java.util.HashSet;

public class DummyAIClient extends GameClient {
    public DummyAIClient(int player_id) {
        super(player_id);
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void UpdateClientChessBoard(ChessBoardStatus cbs, Movement movement, HashSet<Integer> backPlanes, boolean isSkipped, boolean isInitialize) {

    }

    @Override
    public void Dice(DiceType diceType, int dice_count, int dice_result) {

    }

    @Override
    public void ShowOtherDiceResult(int player_id, DiceType diceType, int dice_count, int dice_result) {

    }

    @Override
    public void AnnounceWin(int winner) {

    }

    @Override
    public void AnnounceOtherSkip(int playerID) {

    }

    @Override
    public void AnnounceBattleResult(ChessBoardStatus cbs, Battle battle) {

    }
}
