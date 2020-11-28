package me.luvletter.planechess;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.Timer;
import java.util.TimerTask;

public class formMain {
    public JPanel panel_Main;
    private JPanel panel_Down;
    private JLabel label_Down;
    private JPanel panel_Control;
    private JPanel panel_Center;
    private JPanel panel_dice;
    private JButton btn_dice;
    private JPanel panel_canvas_container_main;
    private JPanel panel_canvas_container_dice;
    private JPanel panel_talk;
    private JPanel panel_talk_bottom;
    private JTextField txt_talk;
    private JTextField textField1;
    private JButton btn_talk_send;
    private JPanel panel_status;
    private JLabel label_status;

    private int counter = 0;

    public Drawable_JPanel dpanel_Main;
    public Drawable_JPanel dpanel_Dice;

    private final Object dicing_lock = new Object();
    private boolean dicing = false;

    private IServer server;

    public formMain(IServer server) {
        super();
        this.server = server;

        dpanel_Main = new Drawable_JPanel();
        dpanel_Dice = new Drawable_JPanel();

        register_Canvas(dpanel_Main, panel_canvas_container_main);
        register_Canvas(dpanel_Dice, panel_canvas_container_dice);

        dpanel_Main.Draw(Resource.getResource(ResourceType.ChessBoard));
        dpanel_Dice.Draw(Resource.getResource(ResourceType.Dice_Unknown));

        btn_dice.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                label_Down.setText("You suck!" + counter++);
                int result = server.rolling_Dice();
                if(!dice_Animation(result))
                    System.out.println("Dicing Rejected!!");
            }
        });
    }

    // Show dicing animation, with result given
    private boolean dice_Animation(int dice_result) {
        synchronized (dicing_lock) {
            if(dicing) return false; // If a dicing task is doing, reject this try
            dicing = true;

            var timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                int count = 0;
                final int total_count = 9; //show 8 animation picture, then the result

                @Override
                public void run() {
                    count++;
                    if (count == total_count) {
                        // the final loop, show result
                        dpanel_Dice.Draw(getResultImage(dice_result));
                        timer.cancel();
                        synchronized (dicing_lock){
                            dicing = false; // reset dicing task flag
                        }
                        return;
                    }
                    // Animation loop
                    dpanel_Dice.Draw(getAnimationImage(count));
                }

                private BufferedImage getAnimationImage(int _count) {
                    return switch (_count) {
                        case 1 -> Resource.getResource(ResourceType.Dice_Rolling1);
                        case 2 -> Resource.getResource(ResourceType.Dice_Rolling2);
                        case 3 -> Resource.getResource(ResourceType.Dice_Rolling3);
                        case 4 -> Resource.getResource(ResourceType.Dice_Rolling4);
                        case 5 -> Resource.getResource(ResourceType.Dice_Rolling5);
                        case 6 -> Resource.getResource(ResourceType.Dice_Rolling6);
                        case 7 -> Resource.getResource(ResourceType.Dice_Rolling7);
                        case 8 -> Resource.getResource(ResourceType.Dice_Rolling8);
                        default -> Resource.getResource(ResourceType.Dice_Unknown);
                    };
                }

                private BufferedImage getResultImage(int result) {
                    return switch (result) {
                        case 1 -> Resource.getResource(ResourceType.Dice_Rolled1);
                        case 2 -> Resource.getResource(ResourceType.Dice_Rolled2);
                        case 3 -> Resource.getResource(ResourceType.Dice_Rolled3);
                        case 4 -> Resource.getResource(ResourceType.Dice_Rolled4);
                        case 5 -> Resource.getResource(ResourceType.Dice_Rolled5);
                        case 6 -> Resource.getResource(ResourceType.Dice_Rolled6);
                        default -> Resource.getResource(ResourceType.Dice_Unknown);
                    };
                }
            }, 50, 200);
            return true;
        }
    }

    // Please make sure `father_container` has BorderLayout!!!!
    // Show https://intellij-support.jetbrains.com/hc/en-us/community/posts/360003406579-Drawing-on-a-JPanel-of-a-form
    private void register_Canvas(Drawable_JPanel dPanel, JPanel father_container) {
        father_container.add(dPanel, BorderLayout.CENTER);
    }

}
