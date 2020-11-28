package me.luvletter.planechess;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.Buffer;
import java.util.HashMap;

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

        var internalServer = new InternalServer();

        var form = new formMain(internalServer);
        var gui = new JFrame("Test App");
        gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gui.setSize(800, 750);
        gui.setContentPane(form.panel_Main);


       // form.dpanel_Main.Draw(Resource.getResource(ResourceType.ChessBoard));
        //form.dpanel_Dice.Draw(Resource.getResource(ResourceType.Dice_Unknown));

        gui.setVisible(true);


    }


}
