package me.luvletter.planechess.game;

import me.luvletter.planechess.event.EndThreadEvent;
import me.luvletter.planechess.game.client.AIClient;
import me.luvletter.planechess.game.client.DummyAIClient;
import me.luvletter.planechess.game.client.GameClient;
import me.luvletter.planechess.game.Game;
import me.luvletter.planechess.server.SocketClient;
import org.java_websocket.WebSocket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Game in serverside
 */
public class ServerGame extends Game {
    public final String UUID;
    public final String RoomName;
    private final HashMap<String, Integer> socketUUIDs = new HashMap<>();

    public ServerGame(String uuid, List<Integer> player_ids, List<Integer> realPlayerIDs, String roomName) {
        super(player_ids.size(), new ArrayList<>(player_ids));
        this.UUID = uuid;
        this.RoomName = roomName;
        // attach AI Clients
        var ai = new ArrayList<>(player_ids);
        ai.removeAll(realPlayerIDs);
        for (int AI_id : ai) {
            this.addClient(new AIClient(AI_id, this));
        }
    }

    public String getRemainPlayerID() {
        var remainList = this.player_ids.stream()
                .filter(pid -> !this.clients.containsKey(pid))
                .collect(Collectors.toList());
        if (remainList.size() == 0)
            return "0";
        return remainList.stream().map(Object::toString).collect(Collectors.joining(", "));
    }

    public boolean attachClientSocket(WebSocket webSocket, int playerID, String socketUUID) {
        if (clients.containsKey(playerID))
            return false;
        if (socketUUIDs.containsKey(webSocket.getAttachment()))
            return false;
        GameClient socketClient = new SocketClient(playerID, webSocket, this);
        this.socketUUIDs.put(socketUUID, playerID);
        addClient(socketClient);
        return true;
    }

    public boolean tryStartGame() {
        if (this.canStart()) {
            this.announceStart();
            return true;
        }
        return false;
    }

    public boolean containSocket(String socketUUID) {
        return this.socketUUIDs.containsKey(socketUUID);
    }

    public SocketClient getSocketClient(String socketUUID) {
        var player_ID = this.socketUUIDs.get(socketUUID);
        var socketClient = this.clients.get(player_ID);
        return (SocketClient) socketClient;
    }

    public List<Integer> getPlayerIDs() {
        return this.player_ids;
    }

    @Override
    public String toString() {
        return "ServerGame{" +
                "UUID='" + UUID + '\'' +
                ", RoomName='" + RoomName + '\'' +
                ", socketUUIDs=" + socketUUIDs +
                ", Player_Count=" + Player_Count +
                ", player_ids=" + player_ids +
                ", clients=" + clients +
                '}';
    }

    public int getClientCount() {
        return this.clients.size();
    }

    /**
     * When a SocketClient is disconnected from server. Then attach a DummyAI to ServerGame
     *
     * @return false if this game has no alive people, just AI and DummyAI, which means this game should be deleted.
     */
    public synchronized boolean socketDisconnect(String socketUUID) {
        Integer player_id = this.socketUUIDs.get(socketUUID);
        var dummyAI = new DummyAIClient(player_id, this);
        this.clients.put(player_id, dummyAI);
        // in ServerGame, there are only three types of Clients: AIClient DummyAIClient and SocketClient
        return this.clients.values().stream().anyMatch(gameClient -> gameClient instanceof SocketClient);
    }

    public boolean socketReconnect() {
        return false;
        // TODO: Reconnect
    }

    public void stop() {
        this.gameEventManager.clearEvents();
        this.gameEventManager.push(new EndThreadEvent());
    }

}
