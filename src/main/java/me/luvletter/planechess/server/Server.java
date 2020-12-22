package me.luvletter.planechess.server;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.*;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static me.luvletter.planechess.util.Utility.generateUUID;

public class Server extends WebSocketServer {

    private final String hostname;
    private final int port;

    /**
     * Key -> gameUUID, Value -> ServerGame
     */
    private volatile HashMap<String, ServerGame> games = new HashMap<>();
    private volatile ArrayList<String> clientUUIDs = new ArrayList<>();
    private final String serverName;

    public Server(String hostname, int port, String serverName) {
        super(new InetSocketAddress(hostname, port));
        this.hostname = hostname;
        this.port = port;
        this.serverName = serverName;
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        try {
            String s = "";
            if (clientHandshake.getContent() != null)
                s = new String(clientHandshake.getContent(), StandardCharsets.UTF_8);
            String uuid = generateUUID();
            webSocket.setAttachment(uuid);
            JSONObject obj = new JSONObject();
            // TODO: Send my Name
            obj.put("action", "hello");
            obj.put("name", this.serverName);
            webSocket.send(obj.toJSONString());
            log("new Connection " + uuid + s + ", From" + webSocket.getRemoteSocketAddress().toString());
        } catch (Exception e) {
            log(" Exception " + e.getClass().toString() + " When handling " + webSocket);
            e.printStackTrace();
        }
    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {
        try {
            String socketUUID = webSocket.getAttachment();
            log(socketUUID + " Disconnected From" + webSocket.getRemoteSocketAddress());
            clientUUIDs.remove(socketUUID);
            if (socketUUID != null) {
                var lst = this.games.values().stream().filter(_sG -> _sG.containSocket(socketUUID)).collect(Collectors.toList());
                if (lst.size() > 0) {
                    var sG = lst.get(0);
                    var isAlive = sG.socketDisconnect(socketUUID);
                    if (!isAlive) {
                        this.games.remove(sG.UUID);
                        sG.stop();
                        log("Game deleted" + sG.UUID);
                    }
                }
            }
            // TODO: clear its game
        } catch (Exception e) {
            log(" Exception " + e.getClass().toString() + " When handling " + webSocket);
            e.printStackTrace();
        }
    }

    @Override
    public void onMessage(WebSocket webSocket, String message) {
        try {
            JSONObject jsonObj;
            try {
                jsonObj = JSONObject.parseObject(message);
            } catch (JSONException jsonException) {
                log("Invalid Messages" + message);
                webSocket.send(BAD_REQUEST);
                return;
            }
            log(JSON.toJSONString(jsonObj, true));
            if (!jsonObj.containsKey("action")) {
                webSocket.send(BAD_REQUEST);
                return;
            }
            if (jsonObj.getString("action").equals("ping")) {
                JSONObject ret = new JSONObject();
                ret.put("status", 200);
                ret.put("action", "ping");
                ret.put("data", "pong!");
                webSocket.send(ret.toJSONString());
                return;
            }
            if (jsonObj.getString("action").equals("list_games")) {
                JSONObject ret = new JSONObject();
                ret.put("status", 200);
                ret.put("action", "list_games");
                var games = this.games.entrySet().stream()
                        .map(entry -> new HashMap<String, String>() {{
                            put("uuid", entry.getKey());
                            put("name", entry.getValue().RoomName);
                            put("remain", entry.getValue().getRemainPlayerID());
                        }})
                        .collect(Collectors.toList());
                ret.put("data", games);
                webSocket.send(ret.toJSONString());
                return;
            }
            if (jsonObj.getString("action").equals("create_game")) {
                String roomName = jsonObj.getString("room_name");
                if (roomName == null) {
                    webSocket.send(BAD_REQUEST);
                    return;
                }
                if (this.games.values().stream().anyMatch(serverGame -> serverGame.RoomName.equals(roomName))) {
                    webSocket.send("{\"status\":403}"); // has the same name
                    return;
                }
                var playerIDs = jsonObj.getJSONArray("player_ids");//.stream().distinct().collect(Collectors.toList());
                if (playerIDs.size() == 0
                        || playerIDs.stream().anyMatch(id -> ((Integer) id < 1 || (Integer) id > 4))) {
                    webSocket.send(BAD_REQUEST);
                    return;
                }
                var realPlayerIDs = jsonObj.getJSONArray("real_player_ids");//.stream().distinct().collect(Collectors.toList());
                if (realPlayerIDs.size() == 0
                        || realPlayerIDs.stream().anyMatch(id -> ((Integer) id < 1 || (Integer) id > 4))
                        || realPlayerIDs.stream().anyMatch(id -> !playerIDs.contains(id))) {
                    webSocket.send(BAD_REQUEST);
                    return;
                }
                // Create Game
                ServerGame serverGame = new ServerGame(generateUUID(),
                        playerIDs.toJavaList(Integer.class).stream().distinct().collect(Collectors.toList()),
                        realPlayerIDs.toJavaList(Integer.class).stream().distinct().collect(Collectors.toList()), roomName);
                this.games.put(serverGame.UUID, serverGame);
                JSONObject ret = new JSONObject();
                ret.put("status", 200);
                ret.put("action", "create_game");
                ret.put("uuid", serverGame.UUID);
                webSocket.send(ret.toJSONString());
                return;
            }
            if (jsonObj.getString("action").equals("join_game")) {
                JSONObject ret = new JSONObject();
                String roomUUID = jsonObj.getString("room_uuid");
                int player_id = jsonObj.getIntValue("player_id");
                if (roomUUID == null) {
                    webSocket.send(BAD_REQUEST);
                    return;
                }
                if (!this.games.containsKey(roomUUID)) {
                    webSocket.send(BAD_REQUEST);
                    return;
                }
                ServerGame game = this.games.get(roomUUID);
                if (game.getSocketClient(webSocket.getAttachment()) != null) {
                    webSocket.send(BAD_REQUEST);
                    return;
                }
                if (!game.getRemainPlayerID().contains(Integer.valueOf(player_id).toString())) {
                    webSocket.send(BAD_REQUEST);
                    return;
                }
                game.attachClientSocket(webSocket, player_id);
                game.tryStartGame();
                ret.put("status", 200);
                ret.put("action", "join_game");
                ret.put("room_uuid", roomUUID);
                ret.put("room_name", this.games.get(roomUUID).RoomName);
                ret.put("player_id", player_id);
                ret.put("players", this.games.get(roomUUID).getPlayerIDs());
                webSocket.send(ret.toJSONString());
            }
            if (jsonObj.getString("action").equals("quit_game")) {
                // TODO: Handle quit_game event
                JSONObject ret = new JSONObject();
                String roomUUID = jsonObj.getString("room_uuid");
                int player_id = jsonObj.getIntValue("player_id");
                if (roomUUID == null) {
                    webSocket.send(BAD_REQUEST);
                    return;
                }
                if (!this.games.containsKey(roomUUID)) {
                    webSocket.send(BAD_REQUEST);
                    return;
                }

                ret.put("status", 418);
                ret.put("action", "quit_game");
                ret.put("room_uuid", roomUUID);
                ret.put("player_id", player_id);
                ret.put("players", this.games.get(roomUUID).getPlayerIDs());
                webSocket.send(ret.toJSONString());
            }
            if (jsonObj.getString("action").equals("game")) {
                if (!jsonObj.containsKey("data")) {
                    webSocket.send(BAD_REQUEST);
                    return;
                }
                String socketUUID = webSocket.getAttachment();
                var lst = this.games.values().stream()
                        .filter(sg -> sg.containSocket(socketUUID)).collect(Collectors.toList());
                if (lst.size() == 0)
                    return;
                lst.get(0).getSocketClient(socketUUID).proceedRequest(jsonObj.getJSONObject("data"));
                return;
            }
            // TODO: proceed None Game actions

        } catch (
                Exception e) {
            log(" Exception " + e.getClass().toString() + " When handling " + webSocket);
            e.printStackTrace();
        }

    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {
        log(webSocket + "Exception: " + e.getClass().toString() + "\n" + Arrays.toString(e.getStackTrace()));
    }

    @Override
    public void onStart() {
        System.out.println("[ServerSocket] Server Start");
    }

    private static void log(String log) {
        System.out.println("[ServerSocket] " + log);
    }

    @Override
    public String toString() {
        return "Server{" +
                "hostname='" + hostname + '\'' +
                ", port=" + port +
                ", games=" + games +
                ", clientUUIDs=" + clientUUIDs +
                ", serverName='" + serverName + '\'' +
                ", clientNumbers=" + this.getConnections().size() +
                '}';
    }

    private static String BAD_REQUEST = "{\"status\" : 400}";
}
