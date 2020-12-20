package me.luvletter.planechess;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import me.luvletter.planechess.client.*;
import me.luvletter.planechess.client.Point;
import me.luvletter.planechess.client.previewing.PreviewAction;
import me.luvletter.planechess.client.previewing.PreviewType;
import me.luvletter.planechess.client.previewing.TakeOffPreviewAction;
import me.luvletter.planechess.event.BattleResultEvent;
import me.luvletter.planechess.event.EventManager;
import me.luvletter.planechess.event.clientevents.*;
import me.luvletter.planechess.server.*;

import static me.luvletter.planechess.client.DiceAnimationHelper.*;
import static me.luvletter.planechess.util.Utility.*;

public class formMain {
    public JPanel panel_Main;
    private JPanel panel_Down;
    private JLabel label_Down;
    private JPanel panel_Control;
    private JPanel panel_Center;
    private JPanel panel_dice1;
    private JPanel panel_canvas_container_main;
    private JPanel panel_canvas_container_dice1;
    private JPanel panel_talk;
    private JPanel panel_talk_bottom;
    private JTextField txt_talk;
    private JTextField textField1;
    private JButton btn_talk_send;
    private JPanel panel_status;
    private JTextArea label_status;
    private JPanel panel_dice2;
    private JPanel panel_canvas_container_dice2;

    public Drawable_JPanel dpanel_Main;
    public Drawable_JPanel dpanel_Dice1;
    public Drawable_JPanel dpanel_Dice2;

    // -1 -> disable dicing
    // 0 -> allow dicing, not started yet
    // 1 -> first dicing finished, waiting for the second
    // 2 -> second dicing finished, wait 5s to reset to Dice_Unknown
    // private int dicing_status = 0;
    private volatile int dice_first_result = 0;     // only under fly mode
    private volatile int dice_second_result = 0;    // only under fly mode
    private volatile int dice_count = 2;
    private volatile DiceType dice_type;
    private volatile boolean isMyDice = false;
    //private volatile boolean dicing = false;
    //private final Object dicing_lock = new Object(); // Dicing lock

    private final Object board_drawing_lock = new Object(); // Main Canvas Drawing Lock

    private LocalClient localClient;
    private final int playerID;

    private EventManager eventManager;
    private Thread ui_thread;

    public formMain(LocalClient localClient, EventManager eventManager) {
        super();
        this.localClient = localClient;
        this.playerID = this.localClient.player_id;
        this.eventManager = eventManager;

        this.label_status.setLineWrap(true);
        this.label_status.setEditable(false);
        this.label_status.setBackground(this.panel_Center.getBackground());

        dpanel_Main = new Drawable_JPanel();
        dpanel_Dice1 = new Drawable_JPanel();
        dpanel_Dice2 = new Drawable_JPanel();

        register_Canvas(dpanel_Main, panel_canvas_container_main);
        register_Canvas(dpanel_Dice1, panel_canvas_container_dice1);
        register_Canvas(dpanel_Dice2, panel_canvas_container_dice2);

        dpanel_Main.Draw(Resource.getResource(ResourceType.ChessBoard));
        dpanel_Dice1.Draw(Resource.getResource(ResourceType.Dice_Unknown));
        dpanel_Dice2.Draw(Resource.getResource(ResourceType.Dice_Unknown));

        //game_server.addCallback_Allow_Dice(this::cb_allow_Dice);
        //game_server.addCallback_Show_Other_Dice(this::cb_show_other_Dice_Animation);
        //game_server.addCallback_update_chessboard(this::cb_update_chessboard);


        dpanel_Main.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                var p = ChessBoardClickHelper.getPointfromMouseEvent(e);
                System.out.println(p);
                eventManager.push(new PreviewEvent(p));

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
                        case Preview -> preview((PreviewEvent) e);
                        case BattleResult -> showBattleResult((BattleResultEvent) e);
//                        case DiceAnimation -> dice_Animation((DiceAnimationEvent) e);
                    }
                    sleep(500);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        ui_thread.start();
    }

    // Events
    private void showDice(DiceEvent e) {
        this.isMyDice = true;

        this.dice_type = e.diceType;
        this.dice_count = e.diceCount;
        // under Fly mode, dice Count is always 2
        this.dice_first_result = e.diceResult / 10;
        this.dice_second_result = e.diceResult % 10;

        dice_Animation(e);
    }

    private void show_other_Dice_Animation(ShowOtherDiceEvent e) {
        this.isMyDice = false;

        this.label_status.setText(PlayerColor.getFriendString(e.playerID) + " is dicing.");
        setJPanelTitle(this.panel_dice1, PlayerColor.getFriendString(e.playerID) + "'s First Dice");
        setJPanelTitle(this.panel_dice2, PlayerColor.getFriendString(e.playerID) + "'s Second Dice");

        diceAnimate(label_status, dpanel_Dice1, getDiceResultinRound(e.diceResult, 1), 1);
        sleep(1000);
        diceAnimate(label_status, dpanel_Dice2, getDiceResultinRound(e.diceResult, 2), 2);

        // final roll
        this.label_status.setText(String.format("Dice ends. %s got %d and %d.", PlayerColor.getFriendString(e.playerID),
                getDiceResultinRound(e.diceResult, 1), getDiceResultinRound(e.diceResult, 2)));
    }

    //private ArrayList<Animation>
    private ChessBoardStatus lastCBS = null;
    private BufferedImage lastImgae = null;

    private void update_chessboard(UpdateChessboardEvent e) {
        if (e.isSkipped) {
            // TODO: Add skipping message
            return;
        }
        ChessBoardStatus cbs = e.cbs;
        if (e.isInitialize) {
            // first draw
            var pst = cbs.getPlanePosition();
            var drawer = new DrawHelper();
            pst.forEach((plane_id, raw_pos) -> {
                drawer.Draw(plane_id, raw_pos);
            });
            dpanel_Main.Draw(drawer.getResultImage());
            // save img for previewing render
            this.lastImgae = drawer.getResultImage();
            System.out.println("first drawing finished!!");
        } else {
            var animation = new Animation(cbs, lastCBS, e.movement, e.backPlanes);
            System.out.println(animation);
            animation.Animate(dpanel_Main);
            lastImgae = animation.FinalDraw(dpanel_Main);
        }
        lastCBS = cbs;
    }

    // Show my dicing animation
    private void dice_Animation(DiceEvent e) {
        this.label_status.setText( "You are dicing.\nGood luck~");
        setJPanelTitle(this.panel_dice1, "Your First Dice");
        setJPanelTitle(this.panel_dice2, "Your Second Dice");

        diceAnimate(label_status, dpanel_Dice1, getDiceResultinRound(e.diceResult, 1), 1);
        sleep(1000);
        diceAnimate(label_status, dpanel_Dice2, getDiceResultinRound(e.diceResult, 2), 2);


        this.label_status.setText(String.format("Dice ends.\nYou got %d and %d.\nYou can choose a plane to move or take off a plane", this.dice_first_result, this.dice_second_result));
    }

    private PreviewAction lastPreview = null;

    private void preview(PreviewEvent e) {
        // TODO: Maybe other click Event?
        if (lastCBS == null || lastImgae == null)
            return;
        if (!this.isMyDice)
            return;
        Point phyPoint = e.clickPoint;
        Position matchPos = ChessBoardClickHelper.matchPositionfromPoint(phyPoint);
        System.out.println(matchPos);
        if (matchPos == null) {
            // TODO : show Talk
            return;
        }
        if (matchPos.ID % 100 == 99) {    // 点击基地
            if (matchPos.Color.getIntValue() != this.playerID)
                return; // 点击别人的基地没有意义
            // try to takeoff
            if ((this.dice_first_result == 6 || this.dice_second_result == 6)
                    && this.lastCBS.getPlanePosition().entrySet().stream()
                    .anyMatch(entry -> entry.getKey() / 10 == this.playerID && entry.getValue() % 100 == 99)) {
                // clear to take off
                this.lastPreview = new TakeOffPreviewAction(this.localClient);
                // Draw Preview Image
                var previewImg = Resource.copyImage(this.lastImgae);
                DrawHelper.drawPlaneWithAlpha(previewImg.createGraphics(), PositionList.all.get(this.playerID * 100).Point,
                        Resource.getPlaneImage(this.playerID), 0.5f);
                this.dpanel_Main.Draw(previewImg);
            }
        }
        if (matchPos.ID % 100 == 0) {
            // 点击起飞处
            if (lastPreview != null)
                if (lastPreview.previewType == PreviewType.TakeOff) {
                    lastPreview.apply();
                    lastPreview = null;
                }
        }
    }

    private void showBattleResult(BattleResultEvent e) {
        Battle result = e.Result;
        ChessBoardStatus now = e.chessBoardStatus;
        System.out.println(result);
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
