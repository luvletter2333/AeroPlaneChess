package me.luvletter.planechess.game.client;

import me.luvletter.planechess.event.clientevents.BattleResultEvent;
import me.luvletter.planechess.event.EventManager;
import me.luvletter.planechess.event.clientevents.*;
import me.luvletter.planechess.game.IGame;
import me.luvletter.planechess.model.Battle;
import me.luvletter.planechess.model.ChessBoardStatus;
import me.luvletter.planechess.model.DiceType;
import me.luvletter.planechess.model.Movement;

import java.util.HashSet;

public class LocalClient extends GameClient {

    private final EventManager clientEventManager;

    public LocalClient(int player_id, IGame game) {
        super(player_id, game);
        clientEventManager = new EventManager();
    }

    public EventManager getClientEventManager() {
        return clientEventManager;
    }

    @Override
    public void UpdateClientChessBoard(ChessBoardStatus cbs, Movement movement, HashSet<Integer> backPlanes, boolean isSkipped, boolean isInitialize) {
        clientEventManager.push(new UpdateChessboardEvent(cbs, movement, backPlanes, isSkipped, isInitialize));
    }

    @Override
    public void Dice(DiceType diceType, int dice_count, int dice_result) {
        clientEventManager.push(new DiceEvent(diceType, dice_count, dice_result));
    }

    @Override
    public void ShowOtherDiceResult(int player_id, DiceType diceType, int dice_count, int dice_result) {
        clientEventManager.push(new ShowOtherDiceEvent(player_id, diceType, dice_count, dice_result));

    }

    @Override
    public void AnnounceWin(int winner) {
        clientEventManager.push(new AnnounceWinEvent(winner));
    }

    @Override
    public void AnnounceOtherSkip(int playerID) {
        this.clientEventManager.push(new OtherSkipEvent(playerID));
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void AnnounceBattleResult(ChessBoardStatus cbs, Battle battle) {
        clientEventManager.push(new BattleResultEvent(cbs, battle));
    }
}
