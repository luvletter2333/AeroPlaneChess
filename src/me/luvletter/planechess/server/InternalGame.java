package me.luvletter.planechess.server;

import me.luvletter.planechess.event.EventManager;
import me.luvletter.planechess.event.eventargs.AllowDiceEvent;
import me.luvletter.planechess.event.eventargs.ShowOtherDiceEvent;
import me.luvletter.planechess.event.eventargs.UpdateChessboardEvent;

import java.util.ArrayList;
import java.util.HashMap;

public class InternalGame extends Game {

    private EventManager clientEventManager;
    public InternalGame(int player_Count, ArrayList<Integer> player_ids) {
        super(player_Count, player_ids);
        clientEventManager = new EventManager();
    }

    public EventManager getClientEventManager() {
        return clientEventManager;
    }

    @Override
    public int rolling_Dice(int player_id) {
        this.dice_player_id = player_id;
        this.dice_moved = false;
        this.dice_first_result = this.dice_random.nextInt(6) + 1;
        this.dice_second_result = this.dice_random.nextInt(6) + 1;
        return this.dice_first_result * 10 + this.dice_second_result;
    }

    @Override
    public boolean move(int plane_id, int step) {
        if(this.dice_player_id != plane_id / 10) // not your turn!!
            return false;
        if(this.dice_moved)     // you have moved!!
            return false;
        // Anti-Cheat done!
        // validate step
        // TODO: Valid Step with first and second dice result
        //this.cbs.getPlanePosition().get
        this.dice_moved = true;
        return true;
    }

    /**
     * For Test Only
     * */
    public void forceMove(int plane_id, int position, boolean immediate_update){

    }

    @Override
    public ChessBoardStatus getChessboardStatus() {
        return new ChessBoardStatus(this.Player_Count, this.planePosition, this.planeStacks);
    }

    @Override
    public void UpdateClientChessBoard() {
        clientEventManager.push(new UpdateChessboardEvent(getChessboardStatus()));
    }

    @Override
    protected void AllowDice() {
        clientEventManager.push(new AllowDiceEvent());
    }

    @Override
    protected void ShowOtherDiceResult() {
        clientEventManager.push(new ShowOtherDiceEvent(PlayerColor.Red, 13));
    }
}
