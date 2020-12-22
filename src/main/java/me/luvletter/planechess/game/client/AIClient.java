package me.luvletter.planechess.game.client;

import me.luvletter.planechess.model.Battle;
import me.luvletter.planechess.model.ChessBoardStatus;
import me.luvletter.planechess.model.DiceType;
import me.luvletter.planechess.model.Movement;

import java.util.HashSet;

public class AIClient extends GameClient {
    public AIClient(int player_id) {
        super(player_id);
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void UpdateClientChessBoard(ChessBoardStatus cbs, Movement movement, HashSet<Integer> backPlanes, boolean isSkippped, boolean isInitialize) {

    }

    @Override
    public void Dice(DiceType diceType, int dice_count, int dice_result) {
        System.out.printf("[AI] %s %s %s\n", diceType, dice_count, dice_result);
        skip();
    }

    @Override
    public void ShowOtherDiceResult(int player_id, DiceType diceType, int dice_count, int dice_result) {

    }

    @Override
    public void AnnounceOtherSkip(int playerID) {

    }

    @Override
    public void AnnounceWin(int winner) {

    }

    @Override
    public void AnnounceBattleResult(ChessBoardStatus cbs, Battle battle) {

    }
}
