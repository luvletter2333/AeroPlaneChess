package me.luvletter.planechess.server;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import me.luvletter.planechess.game.IGame;
import me.luvletter.planechess.game.client.GameClient;
import me.luvletter.planechess.model.Battle;
import me.luvletter.planechess.model.ChessBoardStatus;
import me.luvletter.planechess.model.DiceType;
import me.luvletter.planechess.model.Movement;
import org.java_websocket.WebSocket;

import java.util.HashSet;

public class SocketClient extends GameClient {

    public final String socketUUID;
    private final WebSocket webSocket;

    public SocketClient(int player_id, WebSocket webSocket, IGame game) {
        super(player_id, game);
        this.socketUUID = webSocket.getAttachment();
        this.webSocket = webSocket;
    }

    @Override
    public boolean isReady() {
        return webSocket.isOpen();
    }

    @Override
    public void UpdateClientChessBoard(ChessBoardStatus cbs, Movement movement, HashSet<Integer> backPlanes, boolean isSkipped, boolean isInitialize) {
        JSONObject json = new JSONObject();
        json.put("action", "UpdateChessboard");
        json.put("cbs", JSON.toJSON(cbs));
        json.put("movement", JSON.toJSON(movement));
        json.put("backPlanes", JSON.toJSON(backPlanes));
        json.put("isSkipped", JSON.toJSON(isSkipped));
        json.put("isInitialize", JSON.toJSON(isInitialize));
        JSONObject wrapper = new JSONObject();
        wrapper.put("action", "game");
        wrapper.put("data", json);
        String finalString = JSON.toJSONString(wrapper, SerializerFeature.WriteMapNullValue);
        System.out.println("[Socket Client] Send:" + finalString);
        webSocket.send(finalString);
    }

    @Override
    public void Dice(DiceType diceType, int dice_count, int dice_result) {
        JSONObject json = new JSONObject();
        json.put("action", "showMyDice");
        json.put("diceType", diceType);
        json.put("dice_count", dice_count);
        json.put("dice_result", dice_result);
        JSONObject wrapper = new JSONObject();
        wrapper.put("action", "game");
        wrapper.put("data", json);
        System.out.println("[Socket Client] Send:" + wrapper.toJSONString());
        webSocket.send(wrapper.toJSONString());
    }

    @Override
    public void ShowOtherDiceResult(int player_id, DiceType diceType, int dice_count, int dice_result) {
        JSONObject json = new JSONObject();
        json.put("action", "ShowOtherDice");
        json.put("player_id", player_id);
        //    json.put("diceType", diceType);
        json.put("dice_count", dice_count);
        json.put("dice_result", dice_result);
        JSONObject wrapper = new JSONObject();
        wrapper.put("action", "game");
        wrapper.put("data", json);
        System.out.println("[Socket Client] Send:" + wrapper.toJSONString());
        webSocket.send(wrapper.toJSONString());
    }

    @Override
    public void AnnounceWin(int winner) {
        JSONObject json = new JSONObject();
        json.put("action", "AnnounceWin");
        json.put("winner", winner);
        JSONObject wrapper = new JSONObject();
        wrapper.put("action", "game");
        wrapper.put("data", json);
        System.out.println("[Socket Client] Send:" + wrapper.toJSONString());
        webSocket.send(wrapper.toJSONString());
    }

    @Override
    public void AnnounceOtherSkip(int playerID) {
        JSONObject json = new JSONObject();
        json.put("action", "OtherSkipEvent");
        json.put("playerID", playerID);
        JSONObject wrapper = new JSONObject();
        wrapper.put("action", "game");
        wrapper.put("data", json);
        System.out.println("[Socket Client] Send:" + wrapper.toJSONString());
        webSocket.send(wrapper.toJSONString());
    }

    @Override
    public void AnnounceBattleResult(ChessBoardStatus cbs, Battle battle) {
        JSONObject json = new JSONObject();
        json.put("action", "BattleResult");
        json.put("cbs", JSON.toJSON(cbs));
        json.put("battle", JSON.toJSON(battle));
        JSONObject wrapper = new JSONObject();
        wrapper.put("action", "game");
        wrapper.put("data", json);
        System.out.println("[Socket Client] Send:" + wrapper.toJSONString());
        webSocket.send(wrapper.toJSONString());
    }

    public void proceedRequest(JSONObject jsonData) {
        String action = jsonData.getString("action");
        try {
            switch (action) {
                case "move" -> {
                    int plane_id = jsonData.getIntValue("plane_id");
                    int step = jsonData.getIntValue("step");
                    boolean go_stack = jsonData.getBooleanValue("go_stack");
                    this.game.move(plane_id, step, go_stack);
                }
                case "skip" -> {
                    this.game.skip(this.player_id);
                }
                case "takeoff" -> {
                    this.game.takeOff(this.player_id);
                }
                case "battle" -> {
                    int plane_id = jsonData.getIntValue("plane_id");
                    int step = jsonData.getIntValue("step");
                    this.game.battle(plane_id, step);
                }
            }
        } catch (JSONException e) {
            System.out.println("[SocketClient] BadMessage From" + this.socketUUID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "SocketClient{" +
                "socketUUID='" + socketUUID + '\'' +
                ", player_id=" + player_id +
                '}';
    }
}
