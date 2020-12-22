package me.luvletter.planechess;

import com.alibaba.fastjson.JSONObject;
import me.luvletter.planechess.model.GameList;
import me.luvletter.planechess.game.client.LocalClient;
import me.luvletter.planechess.server.NetworkClient;
import me.luvletter.planechess.game.RemoteGame;
import org.java_websocket.client.WebSocketClient;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.Timer;
import java.util.stream.Collectors;

public class formClient {
    private JButton btn_join_game;
    private JList list_game;
    private JTextField txt_server_ip;
    private JButton btn_conneect_server;
    private JButton btn_create_new_game;
    private JList list1;
    private JLabel label_server_status;
    private JTextField txt_Name;
    private JLabel label_server_name;
    private JPanel form_Server;
    private JButton btn_refresh_list;

    private NetworkClient networkClient;
    private volatile boolean isConnected = false;
    private List<GameList> gameList;

    private boolean isInGame = false;
    private boolean isConnecting = false;
    private formGame formGame;
    private String serverName = "Test";
    private Timer timer;

    private JFrame gui;

    public static void main(String[] args) {
        new formClient();
        new formClient();
    }

    public formClient() {
        this.gameList = new ArrayList<>();
        this.timer = new Timer("gameListRefreshTimer");
        this.btn_conneect_server.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (txt_server_ip.getText().equals(""))
                    return;
                if (txt_Name.getText().equals("")) {
                    return;
                }
                if (!isConnected && !isConnecting) {
                    String uri = "ws://" + txt_server_ip.getText() + ":11451";
                    String myName = txt_Name.getText();
                    try {
                        networkClient = new NetworkClient(new URI(uri), myName);
                        bindUI();
                    } catch (URISyntaxException uriSyntaxException) {
                        JOptionPane.showMessageDialog(null, "invalid IP", "Error", JOptionPane.ERROR_MESSAGE);
                        uriSyntaxException.printStackTrace();
                        return;
                    }
                    for (int i = 0; i < 25; i++) {
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException interruptedException) {
                            interruptedException.printStackTrace();
                        }
                        if (networkClient.isConnected()) {
                            // Connect successfully
                            isConnected = true;
                            label_server_status.setText("Connected");
                            networkClient.setDisconnectedCallback(() -> {
                                isConnected = false;
                                label_server_status.setText("Disconnected");
                                list_game.removeAll();
                            });
                            networkClient.requestRefreshGameList();
//                            timer.schedule(new TimerTask() {
//                                @Override
//                                public void run() {
//                                    networkClient.requestRefreshGameList();
//                                }
//                            }, 1000, 10000);
                            break;
                        }
                    }
                    isConnecting = false;
                }
            }
        });
        gui = new JFrame("Test App");
        gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gui.setSize(800, 750);
        gui.setContentPane(this.form_Server);
        gui.setVisible(true);
        gui.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                networkClient.close();
                gui.setVisible(false);
            }
        });
        btn_refresh_list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                networkClient.requestRefreshGameList();
            }
        });
        btn_create_new_game.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Create a new game
                super.mouseClicked(e);
                String roomName = JOptionPane.showInputDialog(null,
                        "Room Name:", serverName + " - Create a New Game", JOptionPane.QUESTION_MESSAGE);
                String playerIDs = JOptionPane.showInputDialog(null,
                        "Player IDs(split by English comma):", serverName + " - Create a New Game", JOptionPane.QUESTION_MESSAGE);
                String realPlayerIDs = JOptionPane.showInputDialog(null,
                        "Real Player IDs(split by English comma):", serverName + " - Create a New Game", JOptionPane.QUESTION_MESSAGE);
                List<Integer> playerIDs_int, realPlayerIDs_int;
                try {
                    playerIDs_int = Arrays.stream(playerIDs.split(",")).map(Integer::parseInt).collect(Collectors.toList());
                    realPlayerIDs_int = Arrays.stream(realPlayerIDs.split(",")).map(Integer::parseInt).collect(Collectors.toList());
                } catch (Exception ex) {
                    return;
                }
                if (playerIDs_int.stream().anyMatch(id -> id < 1 || id > 4) ||
                        realPlayerIDs_int.stream().anyMatch(id -> id < 1 || id > 4)
                        || realPlayerIDs_int.stream().anyMatch(id -> !playerIDs_int.contains(id))) {
                    return;
                }
                JSONObject req = new JSONObject();
                req.put("action", "create_game");
                req.put("player_ids", playerIDs_int);
                req.put("real_player_ids", realPlayerIDs_int);
                req.put("room_name", roomName);
                networkClient.sendData(req.toJSONString());
            }
        });
        btn_join_game.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                int index = list_game.getSelectedIndex();
                if (index < 0)
                    return;
                GameList gL = gameList.get(index);
                if (gL == null)
                    return;
                if (gL.remain.equals("0"))
                    return;
                Integer ret;
                try {
                    ret = Integer.parseInt(JOptionPane.showInputDialog(null,
                            "Please select an Player", serverName + " - " + gL.name, JOptionPane.QUESTION_MESSAGE));
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return;
                }
                if (!gL.remain.contains(ret.toString())) {
                    JOptionPane.showMessageDialog(null, "invalid Input");
                    return;
                }
                JSONObject req = new JSONObject();
                req.put("action", "join_game");
                req.put("room_uuid", gL.uuid);
                req.put("player_id", ret);
                networkClient.sendData(req.toJSONString());
            }
        });
    }

    private void bindUI() {
        networkClient.bindUI(this);
    }

    public void refreshGameList(List<GameList> gamelist) {
        this.gameList = new ArrayList<>(gamelist);
        this.list_game.removeAll();
        Vector<String> list = this.gameList.stream().map(gL -> gL.name + " - " + gL.remain).collect(Collectors.toCollection(Vector::new));
        this.list_game.setListData(list);
        System.out.println("Game list updated");
    }

    public void showServername(String serverName) {
        this.serverName = serverName;
        this.label_server_name.setText(serverName);
        this.gui.setTitle("飞行棋 - " + serverName);
    }

    public LocalClient startGame(int player_id, String room_name, WebSocketClient webSocketClient) {
        //     this.localClient.bindGame()
        var remoteGame = new RemoteGame(webSocketClient);
        var localClient = new LocalClient(player_id, remoteGame);
        this.formGame = new formGame(localClient, localClient.getClientEventManager());
        this.formGame.setTitle(this.serverName + " - " + room_name);
        this.formGame.showWindow();
        this.formGame.setOnClose(() -> {
            this.networkClient.sendData("{\"action\":\"quit_game\"}");
        });
        return localClient;
    }
}


