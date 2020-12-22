package me.luvletter.planechess.game;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import me.luvletter.planechess.event.EventManager;
import me.luvletter.planechess.formClient;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class NetworkClient {
    private String name;

    private WebSocketClient webSocketClient;

    private formClient UI;
    private LocalClient localGameClient;


    public NetworkClient(URI uri, String name) throws URISyntaxException {
        this.name = name;
        this.webSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                System.out.println("[NetworkClient] Connection established.");
            }

            @Override
            public void onMessage(String message) {
                try {
                    JSONObject jsonObj;
                    try {
                        jsonObj = JSONObject.parseObject(message);
                    } catch (JSONException jsonException) {
                        System.out.println("Invalid Messages" + message);
                        return;
                    }
                    if (!jsonObj.containsKey("action")) {
                        System.out.println("Invalid Messages" + message);
                        return;
                    }
                    String action = jsonObj.getString("action");
                    switch (action) {
                        case "hello" -> UI.showServername(jsonObj.getString("name"));
                        case "list_games" -> UI.refreshGameList(jsonObj.getJSONArray("data").toJavaList(GameList.class));
                        case "create_game" -> this.send("{\"action\":\"list_games\"}"); // reload game lists in UI
                        case "join_game" -> {
                            localGameClient = UI.startGame(jsonObj.getIntValue("player_id"),
                                    jsonObj.getString("room_name"), webSocketClient);

                        }
                        case "quit_game" -> {
                            localGameClient = null;
                        }
                        case "game" -> {
                            if (localGameClient == null)
                                return;
                            JSONObject jsonData = jsonObj.getJSONObject("data");
                            String gameAction = jsonData.getString("action");
                            System.out.println(JSON.toJSONString(jsonData, true));
                            switch (gameAction) {
                                case "UpdateChessboard" -> {
                                    ChessBoardStatus cbs = jsonData.getJSONObject("cbs").toJavaObject(ChessBoardStatus.class);
                                    Movement movement = jsonData.getJSONObject("movement") == null ?
                                            null : jsonData.getJSONObject("movement").toJavaObject(Movement.class);
                                    HashSet<Integer> backPlanes = jsonData.getJSONArray("backPlanes") == null ?
                                            null : new HashSet<Integer>(jsonData.getJSONArray("backPlanes").toJavaList(Integer.class));
                                    boolean isSkipped = jsonData.getBoolean("isSkipped");
                                    boolean isInitialize = jsonData.getBoolean("isInitialize");
                                    localGameClient.UpdateClientChessBoard(cbs, movement, backPlanes, isSkipped, isInitialize);
                                }
                                case "showMyDice" -> {
                                    int dice_count = jsonData.getIntValue("dice_count");
                                    int dice_result = jsonData.getIntValue("dice_result");
                                    localGameClient.Dice(DiceType.Fly, dice_count, dice_result);
                                }
                                case "ShowOtherDice" -> {
                                    int dice_count = jsonData.getIntValue("dice_count");
                                    int dice_result = jsonData.getIntValue("dice_result");
                                    int player_id = jsonData.getIntValue("player_id");
                                    localGameClient.ShowOtherDiceResult(player_id, DiceType.Fly, dice_count, dice_result);
                                }
                                case "AnnounceWin" -> {
                                    int winner = jsonData.getIntValue("winner");
                                    localGameClient.AnnounceWin(winner);
                                }
                                case "OtherSkipEvent" -> {
                                    int playerID = jsonData.getIntValue("playerID");
                                    localGameClient.AnnounceWin(playerID);
                                }
                                case "BattleResult" -> {
                                    ChessBoardStatus cbs = jsonData.getJSONObject("cbs").toJavaObject(ChessBoardStatus.class);
                                    Battle battle = jsonData.getJSONObject("battle").toJavaObject(Battle.class);
                                    localGameClient.AnnounceBattleResult(cbs, battle);
                                }
                            }
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                System.out.println("[NetworkClient] Connection closed.");
                if (disconnectedCallback != null)
                    disconnectedCallback.run();
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        };

        this.webSocketClient.connect();
    }

    public void bindUI(formClient ui) {
        this.UI = ui;
    }

    public void close() {
        this.webSocketClient.close();
    }

    public void requestRefreshGameList() {
        if (this.webSocketClient.isOpen())
            this.webSocketClient.send("{\"action\":\"list_games\"}");
    }

    public boolean isConnected() {
        if (this.webSocketClient == null)
            return false;
        if (this.webSocketClient.isOpen())
            return true;
        return false;
    }

    private Runnable disconnectedCallback;

    public void setDisconnectedCallback(Runnable disconnectedCallback) {
        this.disconnectedCallback = disconnectedCallback;
    }

    public void sendData(String data) {
        this.webSocketClient.send(data);
    }
}
