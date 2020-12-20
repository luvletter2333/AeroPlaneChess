package me.luvletter.planechess;

import me.luvletter.planechess.client.Resource;
import me.luvletter.planechess.server.AIClient;
import me.luvletter.planechess.server.Game;
import me.luvletter.planechess.server.LocalClient;

import javax.swing.*;
import java.util.*;
import java.util.Timer;

public class Main {

    public static final boolean DEBUG_MODE = true;

    public static void main(String[] args) {


        //   var gui = new JFrame("Test");
        //  gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //   gui.setSize(800, 600);
        // var MainLayout = new BorderLayout();
        //  gui.setLayout(new BorderLayout(30, 30));
        //  var Down_Label = new Label();
        //  Down_Label.setText("Test");
        //Down_Label
        //  gui.add(Down_Label, BorderLayout.SOUTH);
        //  gui.setVisible(true);
        //  JOptionPane.showMessageDialog(null,"you suckK");

        // Load Resources
        Resource.loadResources();

        var tmp_player_list = new ArrayList<Integer>();
        tmp_player_list.add(1);
        tmp_player_list.add(2);
        //   tmp_player_list.add(3);
        //   tmp_player_list.add(4);

        // create a new game

        var game = new Game(2, tmp_player_list);

        var client = new LocalClient(1);
        game.addClient(client);
        game.addClient(new AIClient(2));
        //  game.addClient(new AIClient(3));
        // game.addClient(new AIClient(4));

        var form = new formMain(client, client.getClientEventManager());
        var gui = new JFrame("Test App");
        gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gui.setSize(800, 750);
        gui.setContentPane(form.panel_Main);

        gui.setVisible(true);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                game.announceStart();
            }
        }, 1500);

        String str;
        var input = new Scanner(System.in);

        while (true) {
            str = input.nextLine();
            try {
                var cmds = str.split(" ");
                switch (cmds[0]) {
                    case "forceDice" -> game.testforceModifyDiceResult(Int(cmds[1]), Int(cmds[2]));
                    case "forceMoveTo" -> game.testforceMoveTo(Int(cmds[1]), Int(cmds[2]));
                    case "forceMove" -> game.testforceMove(Int(cmds[1]), Int(cmds[2]), Boolean.parseBoolean(cmds[3]));
                    case "move" -> game.move(Int(cmds[1]), Int(cmds[2]), Boolean.parseBoolean(cmds[3]));
                    case "takeoff" -> game.takeOff(Int(cmds[1]));
                    case "status" -> game.testStatus();
                    case "skip" -> game.testSkip(Int(cmds[1]));
                    case "nextloop" -> game.testNextLoop();
                    case "restart" -> game.testReStart();
                    case "cheatDice" -> {
                        var queue = new ArrayDeque<Integer>();
                        for (int i = 1; i < cmds.length; i++)
                            queue.offer(Int(cmds[i]));
                        game.testCheatDice(queue);
                    }
                    case "battle" -> game.battle(Int(cmds[1]), Int(cmds[2]));
                    case "updateUI" ->game.testUpdateUI();
                }
                System.out.println("[Server] run debug command: " + str);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static int Int(String ins) {
        return Integer.parseInt(ins);
    }


}
