package me.luvletter.planechess;

import me.luvletter.planechess.client.*;
import me.luvletter.planechess.client.Point;
import me.luvletter.planechess.eventargs.Show_Other_Dice_EventArg;
import me.luvletter.planechess.server.ChessBoardStatus;
import me.luvletter.planechess.server.Game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Timer;
import java.util.TimerTask;

import static me.luvletter.planechess.client.DrawHelper.drawPlane;

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

    public Drawable_JPanel dpanel_Main;
    public Drawable_JPanel dpanel_Dice;

    // -1 -> disable dicing
    // 0 -> allow dicing, not started yet
    // 1 -> first dicing finished, waiting for the second
    // 2 -> second dicing finished, wait 5s to reset to Dice_Unknown
    // private int dicing_status = 0;
    private int dice_round = 0;
    private int first_dice_result = 0;
    private int second_dice_result = 0;
    private boolean dicing = false;
    private final Object dicing_lock = new Object(); // Dicing lock

    private final Object board_drawing_lock = new Object(); // Main Canvas Drawing Lock

    private Game game_server;

    public formMain(Game game_server) {
        super();
        this.game_server = game_server;

        dpanel_Main = new Drawable_JPanel();
        dpanel_Dice = new Drawable_JPanel();

        register_Canvas(dpanel_Main, panel_canvas_container_main);
        register_Canvas(dpanel_Dice, panel_canvas_container_dice);

        dpanel_Main.Draw(Resource.getResource(ResourceType.ChessBoard));
        dpanel_Dice.Draw(Resource.getResource(ResourceType.Dice_Unknown));

        game_server.addCallback_Allow_Dice(this::cb_allow_Dice);
        game_server.addCallback_Show_Other_Dice(this::cb_show_other_Dice_Animation);
        game_server.addCallback_update_chessboard(this::cb_update_chessboard);

        //    btn_dice.setEnabled(false);
        btn_dice.addActionListener(actionEvent -> {
            btn_dice.setEnabled(false);
            dice_round = 1;
            int result = game_server.rolling_Dice();
            first_dice_result = result / 10;
            second_dice_result = result % 10;

            label_Down.setText("Dicing Round 1! Good luck~");
            dice_Animation(first_dice_result, () -> {
                label_Down.setText("Dicing Round 2 is coming~");
                // First round finished
                dice_round = 2;
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        dice_Animation(second_dice_result, () -> {
                            label_Down.setText("Dicing Round 2 ends.");
                            label_status.setText(convertToMultiline(
                                    "Round 1: " + first_dice_result +
                                            "\nRound 2: " + second_dice_result +
                                            "\nPlease click your plane to fly~"));
                        });
                    }
                }, 3000);
            });
        });

        dpanel_Main.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                var p = ChessBoardClickHelper.getPointfromMouseEvent(e);
                System.out.println(p);
                var ps = ChessBoardClickHelper.matchPositionfromPoint(p);
                //   System.out.println(ps);
            }
        });
    }

    // Callback for server
    private void cb_allow_Dice() {
        //  dicing_status = 0;
        first_dice_result = 0;
        second_dice_result = 0;
        dice_round = 1;
        btn_dice.setEnabled(true);
    }

    private Object cb_show_other_Dice_Animation(Show_Other_Dice_EventArg args) {
        return null;
    }


    private ChessBoardStatus last_cbs;
 //   private ArrayList<Animation>
    private Object cb_update_chessboard(ChessBoardStatus cbs) {
        BufferedImage back = Resource.copyImage(Resource.getResource(ResourceType.ChessBoard));
        var g = back.getGraphics();
        var pst = cbs.getPosition();
        var hangerDrawHelper = new HangerDrawHelper();

        pst.forEach((key, rpos) -> {
            System.out.println(key + " " + rpos);
            // key -> 24 means the fourth plane of player 2
            final int player = key / 10; // from 1 to 4
            final Point pos = (rpos % 100 == 0) ? hangerDrawHelper.getPoint(rpos)
                    : switch (player) {
                    case 1 -> PositionList.RedPositions.get(rpos).Point;
                    case 2 -> PositionList.YellowPositions.get(rpos).Point;
                    case 3 -> PositionList.BluePositions.get(rpos).Point;
                    case 4 -> PositionList.GreenPositions.get(rpos).Point;
                    default -> null;
                };
            System.out.println(pos);
            drawPlane(g, pos, player);
        });
        dpanel_Main.Draw(back);
        return null;
    }


    // Show dicing animation, with result given, dice_result -> [1,6]
    private void dice_Animation(int dice_result, Runnable finish_callback) {
        synchronized (dicing_lock) {
            if (dicing) return; // If a dicing task is doing, reject this try
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
                        label_status.setText(convertToMultiline("Round " + dice_round + " ends.\nYou get " + dice_result + "!"));
                        timer.cancel();
                        synchronized (dicing_lock) {
                            dicing = false; // reset dicing task flag
                        }
                        if (finish_callback != null)
                            finish_callback.run();
                        return;
                    }
                    label_status.setText(convertToMultiline("Round " + dice_round + ".\nYou are dicing" + ".".repeat(count % 3 + 2)));
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
        }
    }

    // Please make sure `father_container` has BorderLayout!!!!
    // Show https://intellij-support.jetbrains.com/hc/en-us/community/posts/360003406579-Drawing-on-a-JPanel-of-a-form
    private void register_Canvas(Drawable_JPanel dPanel, JPanel father_container) {
        father_container.add(dPanel, BorderLayout.CENTER);
    }

    private static String convertToMultiline(String orig) {
        return "<html>" + orig.replaceAll("\n", "<br>");
    }

}
