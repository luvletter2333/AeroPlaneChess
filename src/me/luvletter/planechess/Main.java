package me.luvletter.planechess;

import me.luvletter.planechess.client.Resource;
import me.luvletter.planechess.server.ChessBoardStatus;
import me.luvletter.planechess.server.InternalGame;

import javax.swing.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TimerTask;

public class Main {

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
        var internalServer = new InternalGame(2, tmp_player_list);


        var form = new formMain(internalServer, internalServer.getClientEventManager());
        var gui = new JFrame("Test App");
        gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gui.setSize(800, 750);
        gui.setContentPane(form.panel_Main);

        gui.setVisible(true);

        new java.util.Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                internalServer.UpdateClientChessBoard(internalServer.getChessboardStatus());
            }
        }, 1500);

        new java.util.Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                var cb = internalServer.getChessboardStatus();
                cb.getPlanePosition().remove(33);
                cb.getPlanePosition().put(33,303);
                internalServer.UpdateClientChessBoard(cb);
                // TODO: use forceMove to debug
            }
        }, 4500);

    }


}
