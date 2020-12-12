package me.luvletter.planechess;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import me.luvletter.planechess.client.*;
import me.luvletter.planechess.event.EventManager;
import me.luvletter.planechess.event.clientevents.DiceEvent;
import me.luvletter.planechess.event.clientevents.DiceAnimationEvent;
import me.luvletter.planechess.event.clientevents.ShowOtherDiceEvent;
import me.luvletter.planechess.event.clientevents.UpdateChessboardEvent;
import me.luvletter.planechess.server.ChessBoardStatus;
import me.luvletter.planechess.server.DiceType;
import me.luvletter.planechess.server.LocalClient;

import static me.luvletter.planechess.client.DiceAnimationHelper.*;

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
    private volatile int dice_round = 0;
    private volatile int dice_first_result = 0;     // only under fly mode
    private volatile int dice_second_result = 0;    // only under fly mode
    private volatile int dice_count = 2;
    private volatile DiceType dice_type;
    //private volatile boolean dicing = false;
    //private final Object dicing_lock = new Object(); // Dicing lock

    private final Object board_drawing_lock = new Object(); // Main Canvas Drawing Lock

    private LocalClient localClient;

    private EventManager eventManager;
    private Thread ui_thread;


    public formMain(LocalClient localClient, EventManager eventManager) {
        super();
        this.localClient = localClient;
        this.eventManager = eventManager;

        dpanel_Main = new Drawable_JPanel();
        dpanel_Dice = new Drawable_JPanel();

        register_Canvas(dpanel_Main, panel_canvas_container_main);
        register_Canvas(dpanel_Dice, panel_canvas_container_dice);

        dpanel_Main.Draw(Resource.getResource(ResourceType.ChessBoard));
        dpanel_Dice.Draw(Resource.getResource(ResourceType.Dice_Unknown));

        //game_server.addCallback_Allow_Dice(this::cb_allow_Dice);
        //game_server.addCallback_Show_Other_Dice(this::cb_show_other_Dice_Animation);
        //game_server.addCallback_update_chessboard(this::cb_update_chessboard);

        //    btn_dice.setEnabled(false);
        btn_dice.addActionListener(actionEvent -> {
            System.out.println("disable button, show directly");
            return;

            // TODO: Edit me
//            int result =0; // this.localClient.rolling_Dice(1);
//            if(result == 0) {
//                System.out.println("last player don't dice and move");
//                return;
//            }
//            int first = result / 10;
//            int second = result % 10;
//            this.eventManager.push(new DiceAnimationEvent(first, second));
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

        ui_thread = new Thread(() -> {
            me.luvletter.planechess.event.Event e;
            while (true) {
                try {
                    e = eventManager.get();
                    System.out.println("UI Update Event: Remaining " + eventManager.size() + ", this: " + e.toString());
                    switch (e.getType()) {
                        case showDice -> showDice((DiceEvent) e);
                        case ShowOtherDice -> show_other_Dice_Animation((ShowOtherDiceEvent) e);
                        case UpdateChessboard -> update_chessboard((UpdateChessboardEvent) e);
                        case DiceAnimation -> dice_Animation((DiceAnimationEvent) e);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        ui_thread.start();
    }

    // Events
    private void showDice(DiceEvent e) {
        // TODO: DiceType -> Fly or Battle

        if (e.diceType == DiceType.Fly) {
            this.dice_type = e.diceType;
            this.dice_count = e.diceCount;
            // under Fly mode, dice Count is always 2
            this.dice_first_result = e.diceResult / 10;
            this.dice_second_result = e.diceResult % 10;
            this.eventManager.push(new DiceAnimationEvent(e.diceResult, 1, 2));
        } else {
            // Battle Mode
            // TODO: Handle Battle Mode Dice
        }
        //  dicing_status = 0;
//        this.first_dice_result = 0;
//        this.second_dice_result = 0;
//        this.dice_round = 1;
//        this.btn_dice.setEnabled(true);
    }

    private void show_other_Dice_Animation(ShowOtherDiceEvent e) {

    }

    private ChessBoardStatus last_cbs;

    //private ArrayList<Animation>
    private void update_chessboard(UpdateChessboardEvent e) {
        ChessBoardStatus cbs = e.cbs;
        if (last_cbs == null) {
            // first draw
            var pst = cbs.getPlanePosition();
            var drawer = new DrawHelper();
            pst.forEach((plane_id, raw_pos) -> {
                drawer.Draw(plane_id, raw_pos);
            });
            dpanel_Main.Draw(drawer.getResultImage());
            last_cbs = cbs;
            System.out.println("first drawing finished!!");
        } else {
            // TODO: second draw with animation
        }
    }

    // Show dicing animation
    private void dice_Animation(DiceAnimationEvent e) {
        if (this.dice_type == DiceType.Fly) {
            //this.btn_dice.setEnabled(false);
            this.label_Down.setText("Dicing Round" + e.round + "! Good luck~");
            this.dice_round = e.round;

            diceAnimate(label_status, dpanel_Dice, getDiceResultinRound(e.result, e.round), e.round);

            if (e.round < e.count) {
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
                this.eventManager.push(new DiceAnimationEvent(e.result, e.round + 1, e.count));
            }
            else{
                // final roll
                label_Down.setText(String.format("Dice ends. You got %d and %d. You can choose plane to move or start a plane",this.dice_first_result, this.dice_second_result));
            }
            // next animation
        } else {
            // TODO: Battle Mode
        }
    }

    private int getDiceResultinRound(int raw_result, int round) {
        return Integer.parseInt(String.valueOf(String.valueOf(raw_result).charAt(round - 1)));
    }

    // Please make sure `father_container` has BorderLayout!!!!
    // Show https://intellij-support.jetbrains.com/hc/en-us/community/posts/360003406579-Drawing-on-a-JPanel-of-a-form
    private void register_Canvas(Drawable_JPanel dPanel, JPanel father_container) {
        father_container.add(dPanel, BorderLayout.CENTER);
    }

}
