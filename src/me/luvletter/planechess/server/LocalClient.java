package me.luvletter.planechess.server;

import me.luvletter.planechess.event.EventManager;
import me.luvletter.planechess.event.clientevents.AnnounceWinEvent;
import me.luvletter.planechess.event.clientevents.DiceEvent;
import me.luvletter.planechess.event.clientevents.ShowOtherDiceEvent;
import me.luvletter.planechess.event.clientevents.UpdateChessboardEvent;

import java.util.HashSet;

public class LocalClient extends Client {

    private final EventManager clientEventManager;

    public LocalClient(int player_id) {
        super(player_id);
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
    public boolean isReady() {
        return true;
    }
}
