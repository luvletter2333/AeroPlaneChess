package me.luvletter.planechess.client;

import me.luvletter.planechess.server.ChessBoardStatus;
import me.luvletter.planechess.server.Movement;
import me.luvletter.planechess.server.PlaneStack;
import me.luvletter.planechess.util.Utility;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Animation {
    private final ChessBoardStatus status;
    private final Movement movement;
    private final HashSet<Integer> backPlanes;
    private final DrawHelper drawHelper;
    private BufferedImage base_image;
    private ChessBoardStatus lastStatus;

    public Animation(ChessBoardStatus status, ChessBoardStatus lastStatus, Movement movement, HashSet<Integer> backPlanes) {
        this.status = status;
        this.lastStatus = lastStatus;
        this.movement = movement;
        this.backPlanes = backPlanes;
        this.drawHelper = new DrawHelper();
        //compare and give out which plane(s) won't be move
        var list = compareStatus();
        System.out.println(list);
        generateBaseImage(list);
        // TODO: DEBUG
        try {
            ImageIO.write(this.base_image, "png", new File("/Users/luvletter/Desktop/test.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Generate Base Image to Reduce drawing time;
     */
    private void generateBaseImage(ArrayList<Integer> not_move_list) {
        not_move_list.forEach(plane_id -> {
            this.drawHelper.Draw(plane_id, this.status.getPlanePosition().get(plane_id));
        });
        this.base_image = this.drawHelper.getResultImage();
    }

    public BufferedImage getBase_image() {
        return base_image;
    }

    /**
     * block thread and make animation on given Drawable JPanel
     */
    public void Animate(Drawable_JPanel dpanel) {
        var plane_img = Resource.getPlaneImage(movement.planeID / 10);
        if (this.movement.startPos % 100 == 99) {   // from hanger
            Point start_point = PositionList.all.containsKey(this.movement.startPos) ?
                    PositionList.all.get(this.movement.startPos).Point :
                    DrawHelper.HangerPoints.get(this.movement.planeID);
            Point end_point = PositionList.all.containsKey(this.movement.endPos) ?
                    PositionList.all.get(this.movement.endPos).Point :
                    DrawHelper.HangerPoints.get(this.movement.planeID);
            smallAnimation(this.base_image, plane_img, this.movement.planeID,
                    start_point, end_point, dpanel);
            return;
            // no back situation
        }
        if (this.movement.endPos % 100 <= 13) {
            // dest in the circle
            circleAnimation(this.movement.startPos, this.movement.endPos, this.movement.keypoint, dpanel);
            return;
        }
        // the end point is in the final approach
        if (this.movement.startPos % 100 <= 13) { // startPos is not in the final approach
            // move to x13 first
            int middle_pos = this.movement.planeID / 10 * 100 + 13;
            circleAnimation(this.movement.startPos,
                    middle_pos,
                    this.movement.keypoint, dpanel);
            // then final move to the dest
            var start_point = PositionList.all.get(middle_pos).Point;
            var end_point = PositionList.all.get(this.movement.endPos).Point;
            smallAnimation(base_image, plane_img, this.movement.planeID, start_point, end_point, dpanel);
            return;
        } else {
            // startPos is already in the final approach
            smallAnimation(base_image, plane_img, this.movement.planeID,
                    PositionList.all.get(this.movement.startPos).Point,
                    PositionList.all.get(this.movement.endPos).Point,
                    dpanel);
            return;
        }
    }

    private void circleAnimation(int startPos, int endPos, List<Integer> keypoint, Drawable_JPanel dpanel) {
        int c = 0;
        int nextPos;
        var plane_img = Resource.getPlaneImage(movement.planeID / 10);
        //var base_img_withBP;
        do {
            nextPos = keypoint.size() <= c ? endPos : keypoint.get(c);
            // draw start -> keyPos this path

            HashSet<Integer> stack = null;
            for (PlaneStack planeStack : this.status.getStacks()) {
                if(planeStack.hasPlane(this.movement.planeID)){
                    // current plane in stack
                    for (PlaneStack lastStatusStack : this.lastStatus.getStacks()) {
                        if(lastStatusStack.hasPlane(this.movement.planeID))
                        {
                            stack =
                        }
                        else{

                        }
                    }
                }
            }
            if (stack == null) {
                stack = new HashSet<Integer>(4);
                stack.add(this.movement.planeID);
            }

            int start_index = PositionList.safeIndexOfCircleBoard(startPos, this.movement.planeID / 10);
            // the start point is the runway
            // fix index
            // change start_point to the one in front of runway
            // index -> in the circle
            int end_index = PositionList.circleBoard.indexOf(nextPos);
            Point start_point = PositionList.all.get(startPos).Point;

            for (int i = nextIndex(start_index); i != nextIndex(end_index); i = nextIndex(i)) {
                Point targetPoint = PositionList.all.get(PositionList.circleBoard.get(i)).Point;
                smallAnimation(base_image, plane_img, stack, start_point, targetPoint, dpanel);
                start_point = targetPoint;
            }

            // TODO: back and stack logic here
            for (int backPlane : this.backPlanes) {
                if (this.lastStatus.getPlanePosition().get(backPlane) == backPlane) {

                }
            }

            startPos = nextPos;
            c++;
            if (nextPos == this.movement.endPos)
                break;
        } while (true);
    }

    private int nextIndex(int index) {
        return (index + 1) % 52;
    }

    private static void smallAnimation(BufferedImage back, BufferedImage plane_img, HashSet<Integer> stack, Point start_point, Point end_point, Drawable_JPanel dpanel) {
        final double STEP = 20;
        final int SLEEP_TIME = 10;
        for (int i = 1; i <= STEP; i++) {
            final BufferedImage animate_img = Resource.copyImage(back);
            Graphics g = animate_img.getGraphics();
            for (Integer pid : stack) {
                DrawHelper.drawPlane(g,
                        new Point(start_point.X + (end_point.X - start_point.X) * (i / STEP),
                                start_point.Y + (end_point.Y - start_point.Y) * (i / STEP)),
                        plane_img, pid);
            }
            dpanel.Draw(animate_img);
            Utility.sleep(SLEEP_TIME);
        }
        final BufferedImage endImg = Resource.copyImage(back);
        for (Integer pid : stack) {
            DrawHelper.drawPlane(endImg.getGraphics(), end_point, plane_img, pid);
        }
        dpanel.Draw(endImg);
    }

    /**
     * Compare start_Plane_Positions with end_Plane_Positions, add into movements
     * Calculate Eat_Movements
     *
     * @return list containing all planes which are not going to move.
     */
    private ArrayList<Integer> compareStatus() {
        var notMoved = new ArrayList<Integer>();
        this.status.getPlanePosition().keySet().forEach(plane -> {
            if (this.movement.planeID != plane && this.backPlanes.stream().noneMatch(backplane -> backplane.equals(plane)))
                notMoved.add(plane);
        });
        return notMoved;
    }


    @Override
    public String toString() {
        return "Animation{" +
                "status=" + status +
                ", movement=" + movement +
                ", backPlanes=" + backPlanes +
                '}';
    }
}
