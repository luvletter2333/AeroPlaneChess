package me.luvletter.planechess.server;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.*;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static me.luvletter.planechess.util.Utility.generateUUID;

public class Server extends WebSocketServer {

    private final String hostname;
    private final int port;
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
            webSocket.send();
            log("new Connection " + uuid + s + ", From" + webSocket.getRemoteSocketAddress().toString());
        } catch (Exception e) {
            log(" Exception " + e.getClass().toString() + " When handling " + webSocket);
            e.printStackTrace();
        }
    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {
        try {
            log(webSocket.getAttachment() + " Disconnected From" + webSocket.getRemoteSocketAddress());
            clientUUIDs.remove(webSocket.getAttachment());
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
                var games = this.games.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().RoomName));
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
                JSONArray playerIDs = jsonObj.getJSONArray("player_ids");
                if (playerIDs.size() == 0
                        || playerIDs.stream().anyMatch(id -> ((Integer) id < 1 || (Integer) id > 4))) {
                    webSocket.send(BAD_REQUEST);
                    return;
                }
                JSONArray realPlayerIDs = jsonObj.getJSONArray("real_player_ids");
                if (realPlayerIDs.size() == 0
                        || realPlayerIDs.stream().anyMatch(id -> ((Integer) id < 1 || (Integer) id > 4))
                        || realPlayerIDs.stream().anyMatch(id -> !playerIDs.contains(id))) {
                    webSocket.send(BAD_REQUEST);
                    return;
                }
                // Create Game
                ServerGame serverGame = new ServerGame(generateUUID(),
                        playerIDs.toJavaList(Integer.class),
                        realPlayerIDs.toJavaList(Integer.class), roomName);
                this.games.put(serverGame.UUID, serverGame);
                JSONObject ret = new JSONObject();
                ret.put("status", 200);
                ret.put("action", "create_game");
                ret.put("uuid", serverGame.UUID);
                webSocket.send(ret.toJSONString());
                return;
            }
            if (jsonObj.getString("action").equals("game")) {
                if (!jsonObj.containsKey("data")) {
                    webSocket.send(BAD_REQUEST);
                    return;
                }
                String socketUUID = webSocket.getAttachment();
                var lst = this.games.values().stream()
                        .filter(sg -> sg.containUUID(socketUUID)).collect(Collectors.toList());
                if (lst.size() == 0)
                    return;
                lst.get(0).getSocketClient(socketUUID).proceedRequest(jsonObj.getJSONObject("data"));
                return;
            }
            // TODO: proceed None Game actions

        } catch (Exception e) {
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

    private static String BAD_REQUEST = "{\"status\" : 400}";
}
