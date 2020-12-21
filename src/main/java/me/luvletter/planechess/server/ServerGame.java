package me.luvletter.planechess.server;

import me.luvletter.planechess.game.AIClient;
import me.luvletter.planechess.game.Client;
import me.luvletter.planechess.game.Game;
import org.java_websocket.WebSocket;

import java.util.ArrayList;
import java.util.List;

public class ServerGame extends Game {
    public final String UUID;
    public final String RoomName;
    private final ArrayList<String> socketUUIDs = new ArrayList<>();

    public ServerGame(String uuid, List<Integer> player_ids, List<Integer> realPlayerIDs, String roomName) {
        super(player_ids.size(), new ArrayList<>(player_ids));
        this.UUID = uuid;
        this.RoomName = roomName;
        // attach AI Clients
        var ai = new ArrayList<>(player_ids);
        ai.removeAll(realPlayerIDs);
        for (int AI_id : ai) {
            this.addClient(new AIClient(AI_id));
        }

    }

    public boolean attachClientSocket(WebSocket webSocket, int playerID) {
        if (clients.containsKey(playerID))
            return false;
        if (socketUUIDs.contains(webSocket.getAttachment()))
            return false;
        Client socketClient = new SocketClient(playerID, webSocket);
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

    public boolean containUUID(String uuid){
        return this.socketUUIDs.contains(uuid);
    }

    public SocketClient getSocketClient(String uuid){
        for (Client client : this.clients.values()) {
            if(client instanceof SocketClient)
                if(((SocketClient) client).socketUUID.equals(uuid))
                    return (SocketClient) client;
        }
        return null;
    }
}
