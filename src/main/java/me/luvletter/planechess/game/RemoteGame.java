package me.luvletter.planechess.game;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;

/**
 * give client to use
 * */
public class RemoteGame implements IGame {
    private final WebSocketClient webSocket;

    public RemoteGame(WebSocketClient webSocket) {
        this.webSocket = webSocket;
    }

    @Override
    public void takeOff(int player_id) {
        JSONObject json = new JSONObject();
        json.put("action", "takeoff");
        JSONObject wrapper = new JSONObject();
        wrapper.put("action", "game");
        wrapper.put("data", json);
        System.out.println("[RemoteGame at Client] Send:" + wrapper.toJSONString());
        webSocket.send(wrapper.toJSONString());

    }

    @Override
    public void skip(int player_id) {
        JSONObject json = new JSONObject();
        json.put("action", "skip");
        JSONObject wrapper = new JSONObject();
        wrapper.put("action", "game");
        wrapper.put("data", json);
        System.out.println("[RemoteGame at Client] Send:" + wrapper.toJSONString());
        webSocket.send(wrapper.toJSONString());
    }

    @Override
    public void move(int plane_id, int step, boolean go_stack) {
        JSONObject json = new JSONObject();
        json.put("action", "move");
        json.put("plane_id", plane_id);
        json.put("step", step);
        json.put("go_stack", go_stack);

        JSONObject wrapper = new JSONObject();
        wrapper.put("action", "game");
        wrapper.put("data", json);
        System.out.println("[RemoteGame at Client] Send:" + wrapper.toJSONString());
        webSocket.send(wrapper.toJSONString());
    }

    @Override
    public void battle(int planeID, int step) {
        JSONObject json = new JSONObject();
        json.put("action", "battle");
        json.put("plane_id", planeID); // WARNING: CHECK SocketClient
        json.put("step", step);

        JSONObject wrapper = new JSONObject();
        wrapper.put("action", "game");
        wrapper.put("data", json);
        System.out.println("[RemoteGame at Client] Send:" + wrapper.toJSONString());
        webSocket.send(wrapper.toJSONString());
    }

    @Override
    public String saveGame() {
        JSONObject json = new JSONObject();
        json.put("action", "save");

        JSONObject wrapper = new JSONObject();
        wrapper.put("action", "game");
        wrapper.put("data", json);
        webSocket.send(wrapper.toJSONString());
        return null;
    }

    @Override
    public boolean loadGame(String json) {
        JSONObject wrapper = new JSONObject();
        wrapper.put("action", "game");
        wrapper.put("data", JSON.parse(json));
        webSocket.send(wrapper.toJSONString());
        return false;
    }
}
