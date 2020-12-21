package me.luvletter.planechess.server;

import me.luvletter.planechess.game.*;
import org.java_websocket.WebSocket;

import java.util.HashSet;

public class SocketClient extends Client {

    public final String socketUUID;
    private final WebSocket webSocket;

    public SocketClient(int player_id, WebSocket webSocket) {
        super(player_id);
        this.socketUUID = webSocket.getAttachment();
        this.webSocket = webSocket;
    }

    @Override
    public boolean isReady() {
        return webSocket.isOpen();
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

    public void proceedRequest(String message) {
        // TODO: JSON Parse and proceed Requests from Clients
    }
}
