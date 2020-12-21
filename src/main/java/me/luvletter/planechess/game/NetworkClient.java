package me.luvletter.planechess.game;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;

public class NetworkClient {
    private String name;

    private WebSocketClient webSocketClient;

    public NetworkClient(URI uri, String name) throws URISyntaxException {
        this.name = name;
        this.webSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                System.out.println("[NetworkClient] Connection established.");
            }

            @Override
            public void onMessage(String s) {

            }

            @Override
            public void onClose(int i, String s, boolean b) {
                System.out.println("[NetworkClient] Connection closed.");
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        };
        this.webSocketClient.connect();

    }

}
