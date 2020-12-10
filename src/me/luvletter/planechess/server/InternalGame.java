package me.luvletter.planechess.server;

import me.luvletter.planechess.event.EventManager;
import me.luvletter.planechess.event.clientevents.AllowDiceEvent;
import me.luvletter.planechess.event.clientevents.AnnounceWinEvent;
import me.luvletter.planechess.event.clientevents.ShowOtherDiceEvent;
import me.luvletter.planechess.event.clientevents.UpdateChessboardEvent;

import java.util.ArrayList;

public class InternalGame extends Game {

    private final EventManager clientEventManager;
    public InternalGame(int player_Count, ArrayList<Integer> player_ids) {
        super(player_Count, player_ids);
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
    public void UpdateClientChessBoard(ChessBoardStatus cbs) {
        clientEventManager.push(new UpdateChessboardEvent(cbs));
    }

    @Override
    protected void AllowDice() {
        clientEventManager.push(new AllowDiceEvent());
    }

    @Override
    protected void ShowOtherDiceResult() {
        clientEventManager.push(new ShowOtherDiceEvent(PlayerColor.Red, 13));
    }

    @Override
    protected void AnnounceWin(int winner) {
        clientEventManager.push(new AnnounceWinEvent(winner));
    }
}
