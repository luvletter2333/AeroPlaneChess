package me.luvletter.planechess;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import me.luvletter.planechess.client.*;
import me.luvletter.planechess.event.EventManager;
import me.luvletter.planechess.event.eventargs.DiceAnimationEvent;
import me.luvletter.planechess.event.eventargs.ShowOtherDiceEvent;
import me.luvletter.planechess.event.eventargs.UpdateChessboardEvent;
import me.luvletter.planechess.server.ChessBoardStatus;
import me.luvletter.planechess.server.Game;

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
    private int dice_round = 0;
    private int first_dice_result = 0;
    private int second_dice_result = 0;
    private boolean dicing = false;
    //private final Object dicing_lock = new Object(); // Dicing lock

    private final Object board_drawing_lock = new Object(); // Main Canvas Drawing Lock

    private Game game_server;

    private EventManager eventManager;
    private Thread ui_thread;



    public formMain(Game game_server, EventManager eventManager) {
        super();
        this.game_server = game_server;
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
            int result = game_server.rolling_Dice(1);
            int first = result / 10;
            int second = result % 10;
            this.eventManager.push(new DiceAnimationEvent(first, second));
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
            while(true) {
                try {
                    e = eventManager.get();
                    switch (e.getType()) {
                        case AllowDice:  allow_Dice(); break;
                        case ShowOtherDiceEvent: show_other_Dice_Animation((ShowOtherDiceEvent) e); break;
                        case UpdateChessboard:  update_chessboard((UpdateChessboardEvent) e); break;
                        case DiceAnimation: dice_Animation((DiceAnimationEvent) e); break;
                    }
                }
                catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        });
        ui_thread.start();
    }

    // Events
    private void allow_Dice() {
        //  dicing_status = 0;
        this.first_dice_result = 0;
        this.second_dice_result = 0;
        this.dice_round = 1;
        this.btn_dice.setEnabled(true);
    }

    private void show_other_Dice_Animation(ShowOtherDiceEvent e) {

    }

    private ChessBoardStatus last_cbs;
    //private ArrayList<Animation>
    private void update_chessboard(UpdateChessboardEvent e) {
        ChessBoardStatus cbs = e.cbs;
        if(last_cbs == null){
            // first draw
            var pst = cbs.getPlanePosition();
            var drawer = new DrawHelper();
            pst.forEach((key, rpos) -> {
                drawer.Draw(key, rpos);
            });
            dpanel_Main.Draw(drawer.getResultImage());
            last_cbs = cbs;
            System.out.println("first drawing finished!!");
        }

    }

    // Show dicing animation
    private void dice_Animation(DiceAnimationEvent e) {
        this.btn_dice.setEnabled(false);
        this.first_dice_result = e.firstResult;
        this.second_dice_result = e.secondResult;
        this.label_Down.setText("Dicing Round 1! Good luck~");
        this.dice_round = 1;
        if (dicing) return; // If a dicing task is doing, reject this try
        dicing = true;

        diceAnimate(label_status, dpanel_Dice, this.first_dice_result, 1);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        }
        diceAnimate(label_status, dpanel_Dice, this.second_dice_result, 2);
        dicing = false;
    }

    // Please make sure `father_container` has BorderLayout!!!!
    // Show https://intellij-support.jetbrains.com/hc/en-us/community/posts/360003406579-Drawing-on-a-JPanel-of-a-form
    private void register_Canvas(Drawable_JPanel dPanel, JPanel father_container) {
        father_container.add(dPanel, BorderLayout.CENTER);
    }

}
