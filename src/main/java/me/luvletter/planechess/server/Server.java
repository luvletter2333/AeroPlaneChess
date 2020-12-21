package me.luvletter.planechess.server;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.*;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

import static me.luvletter.planechess.util.Utility.generateUUID;

public class Server extends WebSocketServer {

    private final String hostname;
    private final int port;
    private final HashMap<String, ServerGame> games = new HashMap<>();
    private final ArrayList<String> clientUUIDs = new ArrayList<>();

    public Server(String hostname, int port) {
        super(new InetSocketAddress(hostname, port));
        this.hostname = hostname;
        this.port = port;
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        String s = new String(clientHandshake.getContent(), StandardCharsets.UTF_8);
        String uuid = generateUUID();
        webSocket.setAttachment(uuid);
        log("new Connection " + uuid + s + ", From" + webSocket.getRemoteSocketAddress().toString());
    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {
        log(webSocket.getAttachment() + " Disconnected From" + webSocket.getRemoteSocketAddress());
        clientUUIDs.remove(webSocket.getAttachment());
        // TODO: clear its game
    }

    @Override
    public void onMessage(WebSocket webSocket, String message) {
        // TODO: proceed None Game actions

        String socketUUID = webSocket.getAttachment();
        var lst = this.games.values().stream().filter(sg -> sg.containUUID(socketUUID)).collect(Collectors.toList());
        if (lst.size() == 0)
            return;
        lst.get(0).getSocketClient(socketUUID).proceedRequest(message);
    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {
        log(webSocket.getAttachment() + "Exception: " + e.getClass().toString() + "\n" + Arrays.toString(e.getStackTrace()));
    }

    @Override
    public void onStart() {
        System.out.println("[ServerSocket] Server Start");
    }


    private static void log(String log) {
        System.out.println("[ServerSocket] " + log);
    }
}
