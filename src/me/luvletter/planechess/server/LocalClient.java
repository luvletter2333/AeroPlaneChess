package me.luvletter.planechess.server;

import me.luvletter.planechess.event.EventManager;
import me.luvletter.planechess.event.clientevents.AnnounceWinEvent;
import me.luvletter.planechess.event.clientevents.ShowOtherDiceEvent;
import me.luvletter.planechess.event.clientevents.UpdateChessboardEvent;

public class LocalClient extends Client {

    private final EventManager clientEventManager;
    public LocalClient(int player_id) {
        super(player_id);
        clientEventManager = new EventManager();
    }

    public EventManager getClientEventManager() {
        return clientEventManager;
    }

    /**
     * For Test Only
     * */
    public void forceMove(int plane_id, int position, boolean immediate_update){

    }

    @Override
    public void UpdateClientChessBoard(ChessBoardStatus cbs, ServerMovement movement) {
        clientEventManager.push(new UpdateChessboardEvent(cbs, movement));
    }

    @Override
    protected void Dice(DiceType diceType, int dice_count, int dice_result) {

    }

    @Override
    protected void ShowOtherDiceResult(int player_id, int dice_result) {
        clientEventManager.push(new ShowOtherDiceEvent(player_id, dice_result));

    }

    @Override
    protected void AnnounceWin(int winner) {
        clientEventManager.push(new AnnounceWinEvent(winner));
    }
}
