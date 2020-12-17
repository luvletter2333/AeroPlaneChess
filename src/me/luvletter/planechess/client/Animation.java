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
import java.util.*;
import java.util.List;

public class Animation {
    private final ChessBoardStatus status;
    private final Movement movement;
    private final HashSet<Integer> backPlanes;
    private final DrawHelper drawHelper;
    private BufferedImage baseImage;
    private BufferedImage clearBaseImage; // without backplanes
    private BufferedImage finalImage;
    private final ChessBoardStatus lastStatus;

    public Animation(ChessBoardStatus status, ChessBoardStatus lastStatus, Movement movement, HashSet<Integer> backPlanes) {
        this.status = status;
        this.lastStatus = lastStatus;
        this.movement = movement;
        this.backPlanes = backPlanes;
        this.drawHelper = new DrawHelper();
        //compare and give out which plane(s) won't be move
        this.status.getPlanePosition().forEach((plane_ID, pos) -> {
            if (this.lastStatus.getPlanePosition().get(plane_ID).equals(pos)
                    && getStackedPlanes(this.status.getStacks(), plane_ID) == null) // not in any stack
                this.drawHelper.Draw(plane_ID, this.status.getPlanePosition().get(plane_ID));   // draw individual planes
        });
        for (PlaneStack stack : this.lastStatus.getStacks()) {
            if (stack.hasPlane(this.movement.planeID))
                continue;
            var lst = new ArrayList<>(stack.getStacked_planes());
            if (this.backPlanes.contains(lst.get(0)))
                continue;
            this.drawHelper.Draw(lst, this.lastStatus.getPlanePosition().get(lst.get(0)));
        }
        this.baseImage = this.drawHelper.getResultImage();
        this.clearBaseImage = Resource.copyImage(this.baseImage);
        // Draw BackPlanes
        this.backPlanes.forEach(bpid ->
                DrawHelper.drawPlane(
                        this.baseImage.getGraphics(),
                        PositionList.all.get(this.lastStatus.getPlanePosition().get(bpid)).Point,
                        Resource.getPlaneImage(bpid / 10),
                        bpid)
        );
        try {
            ImageIO.write(this.baseImage, "png", new File("/Users/luvletter/Desktop/test.png"));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public BufferedImage getBase_image() {
        return baseImage;
    }

    public BufferedImage getFinalImage() {
        return finalImage;
    }

    /**
     * block thread and make animation on given Drawable JPanel
     */
    public void Animate(Drawable_JPanel dpanel) {
        var plane_img = Resource.getPlaneImage(movement.planeID / 10);
        var stack = getStackerPlanesOrGenerate(this.status.getStacks(), this.movement.planeID);
        if (this.movement.startPos % 100 == 99) {   // from hanger
            Point start_point = PositionList.all.containsKey(this.movement.startPos) ?
                    PositionList.all.get(this.movement.startPos).Point :
                    DrawHelper.HangerPoints.get(this.movement.planeID);
            Point end_point = PositionList.all.containsKey(this.movement.endPos) ?
                    PositionList.all.get(this.movement.endPos).Point :
                    DrawHelper.HangerPoints.get(this.movement.planeID);
            smallAnimation(this.baseImage, plane_img, stack,
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
            smallAnimation(baseImage, plane_img, stack, start_point, end_point, dpanel);
            return;
        } else {
            // startPos is already in the final approach
            smallAnimation(baseImage, plane_img, stack,
                    PositionList.all.get(this.movement.startPos).Point,
                    PositionList.all.get(this.movement.endPos).Point,
                    dpanel);
            return;
        }
    }

    public BufferedImage FinalDraw(Drawable_JPanel dpanel) {
        HashSet<Integer> stack = getStackerPlanesOrGenerate(this.status.getStacks(), this.movement.planeID);
        BufferedImage final_img = Resource.copyImage(this.baseImage);
        DrawHelper.drawPlane(
                final_img.getGraphics(),
                PositionList.all.get(this.movement.endPos).Point,
                Resource.getPlaneImage(this.movement.planeID / 10),
                new ArrayList<>(stack));
        var dh = new DrawHelper(final_img);
        this.backPlanes.forEach(bp ->
                dh.Draw(bp, this.status.getPlanePosition().get(bp)));
        this.finalImage = dh.getResultImage();
        dpanel.Draw(this.finalImage);
        return this.finalImage;
    }

    private void circleAnimation(int startPos, int endPos, List<Integer> keypoint, Drawable_JPanel dpanel) {
        int c = 0;
        int nextPos;
        var plane_img = Resource.getPlaneImage(movement.planeID / 10);
        HashSet<Integer> stack = getStackedPlanes(this.lastStatus.getStacks(), this.movement.planeID);
        HashSet<Integer> cur_stack = getStackedPlanes(this.status.getStacks(), this.movement.planeID);
        boolean stack_add_plane = false;
        HashSet<Integer> added_planes = new HashSet<>(4);
        BufferedImage endImg = null;
        HashSet<Integer> backplanes = new HashSet<>(this.backPlanes);
        if (stack == null) {
            stack = new HashSet<>();
            stack.add(this.movement.planeID);
        }
        if (cur_stack != null) {
            stack_add_plane = stack.size() != cur_stack.size();
            added_planes.addAll(cur_stack);
            added_planes.removeAll(stack);
        }
        do {
            nextPos = keypoint.size() <= c ? endPos : keypoint.get(c);
            var base_image = Resource.copyImage(this.baseImage);
            // draw start -> keyPos this path
            if (stack_add_plane) {
                var iterator = added_planes.iterator();
                while (iterator.hasNext()) {
                    var added_plane = iterator.next();
                    if (this.lastStatus.getPlanePosition().get(added_plane) == startPos) {
                        stack.add(added_plane);
                        // added_planes.remove(added_plane);
                        iterator.remove();
                    } else {
                        DrawHelper.drawPlane(base_image.getGraphics(),
                                PositionList.all.get(this.lastStatus.getPlanePosition().get(added_plane)).Point,
                                plane_img,
                                added_plane);
                    }
                }
            }
            System.out.printf("From:%s To: %s Stack:%s need_add_stack:%s\n", startPos, nextPos, stack, added_planes);

            int start_index = PositionList.safeIndexOfCircleBoard(startPos, this.movement.planeID / 10);
            // the start point is the runway
            // fix index
            // change start_point to the one in front of runway
            // index -> in the circle
            int end_index = PositionList.circleBoard.indexOf(nextPos);
            Point start_point = PositionList.all.get(startPos).Point;

            for (int i = nextIndex(start_index); i != nextIndex(end_index); i = nextIndex(i)) {
                Point targetPoint = PositionList.all.get(PositionList.circleBoard.get(i)).Point;
                endImg = smallAnimation(base_image, plane_img, stack, start_point, targetPoint, dpanel);
                start_point = targetPoint;
            }

            Map<Integer, Point> bp_end_points = new HashMap<>();

            // TODO: back logic here
            var iterator = backplanes.iterator();
            while (iterator.hasNext()) {
                int backPlaneID = iterator.next();
                if (this.lastStatus.getPlanePosition().get(backPlaneID).equals(nextPos)) {
                    bp_end_points.put(backPlaneID, DrawHelper.HangerPoints.get(backPlaneID));
                    iterator.remove();
                }
            }

            System.out.println(bp_end_points);
            System.out.println(backplanes);
            if (bp_end_points.size() > 0) {
                smallAnimation(endImg, bp_end_points, PositionList.all.get(nextPos).Point, dpanel);
                var newBaseImage = Resource.copyImage(this.clearBaseImage);
                bp_end_points.keySet().forEach(bpid ->
                        DrawHelper.drawPlane(
                                newBaseImage.getGraphics(),
                                bp_end_points.get(bpid),
                                Resource.getPlaneImage(bpid / 10),
                                bpid)
                ); // has been moved
                backplanes.forEach(bpid ->
                        DrawHelper.drawPlane(
                                newBaseImage.getGraphics(),
                                PositionList.all.get(this.lastStatus.getPlanePosition().get(bpid)).Point,
                                Resource.getPlaneImage(bpid / 10),
                                bpid)
                ); // has not been removed
                this.baseImage = Resource.copyImage(newBaseImage);
            }

            startPos = nextPos;
            c++;
            if (nextPos == this.movement.endPos)
                break;
        } while (true);
    }

    private static HashSet<Integer> getStackedPlanes(List<PlaneStack> planeStacks, int planeID) {
        for (PlaneStack planeStack : planeStacks) {
            if (planeStack.hasPlane(planeID))
                return planeStack.getStacked_planes();
        }
        return null;
    }

    private static HashSet<Integer> getStackerPlanesOrGenerate(List<PlaneStack> planeStacks, int planeID) {
        HashSet<Integer> ret = getStackedPlanes(planeStacks, planeID);
        if (ret == null) {
            ret = new HashSet<>();
            ret.add(planeID);
        }
        return ret;
    }

    private int nextIndex(int index) {
        return (index + 1) % 52;
    }

    private static BufferedImage smallAnimation(BufferedImage back, BufferedImage plane_img, HashSet<Integer> stack, Point start_point, Point end_point, Drawable_JPanel dpanel) {
        final double STEP = 50;
        final int SLEEP_TIME = 10;
        for (int i = 1; i <= STEP; i++) {
            final BufferedImage animate_img = Resource.copyImage(back);
            Graphics g = animate_img.getGraphics();
            DrawHelper.drawPlane(g,
                    new Point(start_point.X + (end_point.X - start_point.X) * (i / STEP),
                            start_point.Y + (end_point.Y - start_point.Y) * (i / STEP)),
                    plane_img, new ArrayList<>(stack));
            dpanel.Draw(animate_img);
            Utility.sleep(SLEEP_TIME);
        }
        final BufferedImage endImg = Resource.copyImage(back);
        DrawHelper.drawPlane(endImg.getGraphics(), end_point, plane_img, new ArrayList<>(stack));
        dpanel.Draw(endImg);
        return endImg;
    }

    private static BufferedImage smallAnimation(BufferedImage back, Map<Integer, Point> planes, Point start_point, Drawable_JPanel dpanel) {
        final double STEP = 50;
        final int SLEEP_TIME = 10;
        for (int i = 1; i <= STEP; i++) {
            final BufferedImage animate_img = Resource.copyImage(back);
            Graphics g = animate_img.getGraphics();
            for (int pid : planes.keySet()) {
                Point point = planes.get(pid);
                DrawHelper.drawPlane(g,
                        new Point(start_point.X + (point.X - start_point.X) * (i / STEP),
                                start_point.Y + (point.Y - start_point.Y) * (i / STEP)),
                        Resource.getPlaneImage(pid / 10), pid);
            }
            dpanel.Draw(animate_img);
            Utility.sleep(SLEEP_TIME);
        }
        final BufferedImage endImg = Resource.copyImage(back);
        planes.forEach((pid, point) -> {
            DrawHelper.drawPlane(endImg.getGraphics(), point, Resource.getPlaneImage(pid / 10), pid);
        });
        dpanel.Draw(endImg);
        return endImg;
    }

    @Override
    public String toString() {
        return "Animation{" +
                "\nstatus=" + status +
                "\nlastStatus=" + lastStatus +
                "\n, movement=" + movement +
                ", backPlanes=" + backPlanes +
                ", drawHelper=" + drawHelper +
                ", base_image=" + baseImage +
                '}';
    }
}
