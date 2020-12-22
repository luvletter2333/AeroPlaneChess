package me.luvletter.planechess;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Collectors;

import me.luvletter.planechess.client.*;
import me.luvletter.planechess.client.Point;
import me.luvletter.planechess.client.previewing.MovePreviewAction;
import me.luvletter.planechess.client.previewing.PreviewAction;
import me.luvletter.planechess.client.previewing.PreviewType;
import me.luvletter.planechess.client.previewing.TakeOffPreviewAction;
import me.luvletter.planechess.event.BattleResultEvent;
import me.luvletter.planechess.event.EventManager;
import me.luvletter.planechess.event.clientevents.*;
import me.luvletter.planechess.game.*;

import static me.luvletter.planechess.client.DiceAnimationHelper.*;
import static me.luvletter.planechess.util.Utility.*;

public class formGame {
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
    private volatile boolean isWin = false;

    private final Object board_drawing_lock = new Object(); // Main Canvas Drawing Lock

    private GameClient localClient;
    private final int playerID;

    private EventManager eventManager;
    private Thread ui_thread;

    private JFrame gui;
    private Runnable onClose;

    public formGame(LocalClient localClient, EventManager eventManager) {
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
                eventManager.push(new PreviewEvent(p));
            }

        });

        ui_thread = new Thread(() -> {
            me.luvletter.planechess.event.Event e;
            while (true) {
                try {
                    e = eventManager.get();
                    System.out.println("[Client UI Event] Remaining " + eventManager.size() + ", this: " + e.toString());
                    switch (e.getType()) {
                        case showMyDice -> showDice((DiceEvent) e);
                        case ShowOtherDice -> show_other_Dice_Animation((ShowOtherDiceEvent) e);
                        case UpdateChessboard -> update_chessboard((UpdateChessboardEvent) e);
                        case Preview -> preview((PreviewEvent) e);
                        case BattleResult -> showBattleResult((BattleResultEvent) e);
//                        case DiceAnimation -> dice_Animation((DiceAnimationEvent) e);
                    }
                    sleep(50);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        ui_thread.start();

        this.gui = new JFrame();

        this.gui.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.gui.setSize(900, 750);
        this.gui.setContentPane(this.panel_Main);
        this.gui.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (!isWin) {
                    if (JOptionPane.showConfirmDialog(gui,
                            "Are you sure you want to quit game?", "Close Window?",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION) {
                        return;
                    }
                }
                // accept to quit
                if (onClose != null) {
                    onClose.run();
                    gui.setVisible(false);
                    gui.dispose();
                }
            }
        });
    }

    public void setTitle(String title) {
        this.gui.setTitle(title);
    }

    public void setOnClose(Runnable onClose) {
        this.onClose = onClose;
    }

    public void showWindow() {
        this.gui.setVisible(true);
    }

    public void bindGame(IGame game) {
        this.localClient.bindGame(game);
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
        if (this.dice_first_result != 6 && this.dice_second_result != 6 &&
                this.lastCBS.getPlanePosition().entrySet().stream()
                        .filter(entry -> entry.getKey() / 10 == this.playerID)
                        .allMatch(entry -> entry.getValue() % 100 == 99)) {
            // 无法takeoff 全在机库 只能skip
            JOptionPane.showMessageDialog(null, "There is nothing else to do but skip this loop~");
            System.out.println("OK");
            localClient.skip();
        }
    }

    private void show_other_Dice_Animation(ShowOtherDiceEvent e) {
        this.isMyDice = false;

        this.label_status.setText(PlayerColor.getFriendString(e.playerID) + " is dicing.");
        setJPanelTitle(this.panel_dice1, PlayerColor.getFriendString(e.playerID) + "'s First Dice");
        setJPanelTitle(this.panel_dice2, PlayerColor.getFriendString(e.playerID) + "'s Second Dice");

        diceAnimate(dpanel_Dice1, getDiceResultinRound(e.diceResult, 1), 1);
        sleep(1000);
        //TODO: Debug
        diceAnimate(dpanel_Dice2, getDiceResultinRound(e.diceResult, 2), 2);

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
            this.dpanel_Main.Draw(drawer.getResultImage());
            // save img for previewing render
            this.lastImgae = drawer.getResultImage();
        } else {
            var animation = new Animation(cbs, lastCBS, e.movement, e.backPlanes);
            //  System.out.println(animation);
            animation.Animate(dpanel_Main);
            this.lastImgae = animation.FinalDraw(dpanel_Main);
        }
        this.lastCBS = cbs;
    }

    // Show my dicing animation
    private void dice_Animation(DiceEvent e) {
        this.label_status.setText("You are dicing.\nGood luck~");
        setJPanelTitle(this.panel_dice1, "Your First Dice");
        setJPanelTitle(this.panel_dice2, "Your Second Dice");

        diceAnimate(dpanel_Dice1, getDiceResultinRound(e.diceResult, 1), 1);
        sleep(1000);
        // TODO: Debug
        diceAnimate(dpanel_Dice2, getDiceResultinRound(e.diceResult, 2), 2);

        this.label_status.setText(
                String.format("Dice ends.\nYou got %d and %d.\nClick a plane to\n take off or move.",
                        this.dice_first_result, this.dice_second_result));
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
                this.lastPreview = new TakeOffPreviewAction(this.playerID, this.localClient.getGame());
                // Draw Preview Image
                var previewImg = Resource.copyImage(this.lastImgae);
                DrawHelper.drawPlaneWithAlpha(previewImg.createGraphics(),
                        PositionList.all.get(this.playerID * 100).Point,
                        Resource.getPlaneImage(this.playerID), 0.5f);
                this.dpanel_Main.Draw(previewImg);
            } else {
                // delete all preview Image
                this.dpanel_Main.Draw(this.lastImgae);
            }
            return;
        }
        if (matchPos.ID % 100 == 0) {
            // 点击起飞处
            if (this.lastPreview != null)
                if (this.lastPreview.previewType == PreviewType.TakeOff) {
                    this.lastPreview.apply();
                    this.lastPreview = null;
                    return;
                }
        }
        if (this.lastPreview != null) {
            if (this.lastPreview.previewType == PreviewType.Move) {
                var previewObj = (MovePreviewAction) this.lastPreview;
                var clickpMlst = previewObj.possibleMove.stream()
                        .filter(pM -> calculateDestPos(this.playerID, previewObj.sourcePos, pM) == matchPos.ID)
                        .collect(Collectors.toList());
                if (clickpMlst.size() > 0) {
                    // click a valid Move dest
                    boolean goStack = JOptionPane.showConfirmDialog(null,
                            "Do you want to form a stack if can?", "PlaneChess",
                            JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
                    // TODO: move or battle
                    //previewObj.apply(clickpMlst.get(0), goStack);
                    if (this.lastCBS.getPlanePosition().entrySet().stream()
                            .anyMatch(entry -> entry.getKey() / 10 != this.playerID && entry.getValue() == matchPos.ID))
                        this.localClient.battle(previewObj.planeID, clickpMlst.get(0));
                    else
                        this.localClient.move(previewObj.planeID, clickpMlst.get(0), goStack);
                    this.lastPreview = null;
                    return;
                }
            }
        }
        var clickPlanesList = this.lastCBS.getPlanePosition().entrySet().stream()
                .filter(entry -> entry.getKey() / 10 == this.playerID)
                .filter(entry -> entry.getValue() == matchPos.ID)
                .filter(entry -> entry.getValue() % 100 != 99)
                .map(entry -> entry.getKey())
                .collect(Collectors.toList());
        if (clickPlanesList.size() > 0) {
            var possibleMove = new ArrayList<Integer>();
            possibleMove.add(dice_first_result + dice_second_result);
            possibleMove.add(dice_first_result * dice_second_result);
            possibleMove.add(Math.abs(dice_first_result - dice_second_result));
            if (dice_first_result % dice_second_result == 0)
                possibleMove.add(dice_first_result / dice_second_result);
            if (dice_second_result % dice_first_result == 0)
                possibleMove.add(dice_second_result / dice_first_result);
            possibleMove.removeIf(pM -> pM > 12);
            int rep = clickPlanesList.get(0);
            var previewImg = Resource.copyImage(this.lastImgae);
            possibleMove.forEach(psMove ->
                    DrawHelper.drawPlaneWithAlpha(previewImg.createGraphics(),
                            PositionList.all.get(calculateDestPos(this.playerID, matchPos.ID, psMove)).Point,
                            Resource.getPlaneImage(this.playerID), 0.5f));
            this.dpanel_Main.Draw(previewImg);

            this.lastPreview = new MovePreviewAction(this.localClient.getGame(), rep, matchPos.ID, possibleMove);
        }
    }

    private void showBattleResult(BattleResultEvent e) {
        Battle battle = e.Result;
        ChessBoardStatus now = e.chessBoardStatus;
        System.out.println(battle);
        // TODO: Make Battle Animation
        var drawHelper = new DrawHelper();
        this.lastCBS.getPlanePosition().keySet().stream()   //所有飞机
                .filter(plnid -> !battle.stack1.contains(plnid))
                .filter(plnid -> !battle.stack2.contains(plnid)) //与这次battle无关
                .forEach(plnid -> drawHelper.Draw(plnid, this.lastCBS.getPlanePosition().get(plnid)));
        var baseImage = drawHelper.getResultImage();
        try {
            ImageIO.write(baseImage, "png", new File("/Users/luvletter/Desktop/battle.png"));

        } catch (IOException ioE) {
            ioE.printStackTrace();
        }
        //生成baseImage

        var friend_name1 = PlayerColor.getFriendString(battle.planeID1 / 10);
        var friend_name2 = PlayerColor.getFriendString(battle.planeID2 / 10);
        String battleTitle = "Battle Between " + friend_name1 + " and " + friend_name2 + "!";
        this.label_status.setText(battleTitle);
        setJPanelTitle(this.panel_dice1, friend_name1 + "'s Dice");
        setJPanelTitle(this.panel_dice2, friend_name2 + "'s Dice");
        // 修改Dice status 标题

        var firstAniImage = Resource.copyImage(baseImage);
        DrawHelper.drawPlane(firstAniImage.getGraphics(), PositionList.all.get(battle.destPosition).Point,
                Resource.getPlaneImage(battle.planeID2 / 10), battle.stack2);

        Animation.smallAnimation(firstAniImage,
                Resource.getPlaneImage(battle.planeID1 / 10),
                battle.stack1,
                PositionList.all.get(this.lastCBS.getPlanePosition().get(battle.planeID1)).Point,
                new Point(PositionList.all.get(battle.destPosition).Point.X + 20, PositionList.all.get(battle.destPosition).Point.Y + 20)
                , dpanel_Main);
        // 把stack1移动到stack2脸上

        var _stack1 = new ArrayList<Integer>(battle.stack1);
        var _stack2 = new ArrayList<Integer>(battle.stack2);
        var backed = new ArrayList<Integer>(4);
        // 所有参与者

        for (BattleResult result : battle.getResults()) {
            // 绘制每一次Battle的动画
            // 先显示Dice动画
            this.label_status.setText(battleTitle + "\n" + friend_name1 + " is dicing...");
            diceAnimate(this.dpanel_Dice1, result.dice1, 1);
            sleep(1000);
            this.label_status.setText(battleTitle + "\n" + friend_name2 + " is dicing...");
            diceAnimate(this.dpanel_Dice2, result.dice2, 1);
            sleep(1000);

            var anmImage = Resource.copyImage(baseImage);
            backed.forEach(bpid -> DrawHelper.drawPlane(anmImage.getGraphics(), DrawHelper.HangerPoints.get(bpid),
                    Resource.getPlaneImage(bpid / 10), bpid));
            // 已经滚回去的

            // 绘制本次Send Back的动画，本次只滚回去一架飞机
            var backPlaneID = result.getWinnerPlaneID() == result.planeID1 ? result.planeID2 : result.planeID1;
            if (_stack1.remove(Integer.valueOf(backPlaneID)))
                backed.add(backPlaneID);
            if (_stack2.remove(Integer.valueOf(backPlaneID)))
                backed.add(backPlaneID);

            if (!_stack1.isEmpty())
                DrawHelper.drawPlane(anmImage.getGraphics(),
                        new Point(PositionList.all.get(battle.destPosition).Point.X + 20, PositionList.all.get(battle.destPosition).Point.Y + 20),
                        Resource.getPlaneImage(_stack1.get(0) / 10), _stack1);
            if (!_stack2.isEmpty())
                DrawHelper.drawPlane(anmImage.getGraphics(), PositionList.all.get(battle.destPosition).Point,
                        Resource.getPlaneImage(_stack2.get(0) / 10), _stack2);
            // 还没有滚回去的

            Animation.smallAnimation(anmImage, Resource.getPlaneImage(backPlaneID / 10),
                    new ArrayList<Integer>() {{
                        add(backPlaneID);
                    }},
                    new Point(PositionList.all.get(battle.destPosition).Point.X + 20, PositionList.all.get(battle.destPosition).Point.Y + 20),
                    DrawHelper.HangerPoints.get(backPlaneID), dpanel_Main);
            // 滚回去的动画
            sleep(500);
        }
        //绘制FinalImage
        // 先绘制已经滚回去的
        backed.forEach(bpid -> DrawHelper.drawPlane(baseImage.getGraphics(),
                DrawHelper.HangerPoints.get(bpid), Resource.getPlaneImage(bpid / 10), bpid));
        //最后把remaining stack移动到destPos
        if (battle.getWinnerPlayerID() == battle.planeID1 / 10)
            this.lastImgae = Animation.smallAnimation(baseImage, Resource.getPlaneImage(battle.remainstack.get(0) / 10),
                    new ArrayList<>() {{
                        addAll(_stack1);
                        addAll(_stack2);
                    }},
                    new Point(PositionList.all.get(battle.destPosition).Point.X + 20, PositionList.all.get(battle.destPosition).Point.Y + 20),
                    PositionList.all.get(battle.destPosition).Point, dpanel_Main);
        else
            this.lastImgae = baseImage;
        this.lastCBS = now;
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
