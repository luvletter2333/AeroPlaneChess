package me.luvletter.planechess.client;

import me.luvletter.planechess.Main;
import me.luvletter.planechess.server.PlaneStack;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import static me.luvletter.planechess.client.Resource.getPlaneImage;

public class DrawHelper {
    /**
     * planeColor: 1,2,3,4 => Red, Yellow, Blue, Green
     *
     * @param plane_id for debug only
     */
    public static void drawPlane(Graphics g, Point phyPoint, BufferedImage plane_img, int plane_id) {
        g.drawImage(plane_img, phyPoint.X - 10, phyPoint.Y - 10, 20, 20, null);
        // scale 30x30 -> 20x20
        if (Main.DEBUG_MODE) {
            // under debug mode, draw plane_ID;
            g.drawString(Integer.valueOf(plane_id).toString(), phyPoint.X + 10, phyPoint.Y + 10);
        }
    }

    /**
     * draw a stack of planes
     * planeColor: 1,2,3,4 => Red, Yellow, Blue, Green
     *
     * @param planes a bunch of planes' ID
     */
    public static void drawPlane(Graphics g, Point phyPoint, BufferedImage plane_img, java.util.List<Integer> planes) {
        for (int i = 0; i < planes.size(); i++) {
            // scale 30x30 -> 20x20
            g.drawImage(plane_img, phyPoint.X - 10 + i * 10, phyPoint.Y - 10 + i * 10, 20, 20, null);
            if (Main.DEBUG_MODE)
                g.drawString(planes.get(i).toString(), phyPoint.X + 10 + i * 10, phyPoint.Y + 10 + i * 10);
            // under debug mode, draw plane_ID;
        }
    }

    private final BufferedImage ResultImage;
    private final Graphics g;

    public DrawHelper() {
        this.ResultImage = Resource.copyImage(Resource.getResource(ResourceType.ChessBoard));
        g = this.ResultImage.getGraphics();
    }

    public DrawHelper(BufferedImage srcImg) {
        this.ResultImage = srcImg;
        this.g = this.ResultImage.createGraphics();
    }

    public BufferedImage getResultImage() {
        return ResultImage;
    }

    public void Draw(int plane_id, int raw_position_id) {
        // key -> 24 means the fourth plane of player 2
        final int player_id = plane_id / 10; // from 1 to 4
        Point point;
        if (raw_position_id % 100 == 99)
            point = HangerPoints.get(plane_id);
        else if (raw_position_id % 100 == 19) {
            point = HangerPoints.get(plane_id);
        } else point = PositionList.all.get(raw_position_id).Point;
        drawPlane(this.g, point, getPlaneImage(plane_id / 10, raw_position_id), plane_id);
    }

    /**
     * raw_position_id shouldn't be 19!!
     */
    public void Draw(java.util.List<Integer> stack, int raw_position_id) {
        final int player_id = stack.get(0) / 10;
        final Point point = switch (raw_position_id / 100) {
            case 1 -> PositionList.RedPositions.get(raw_position_id).Point;
            case 2 -> PositionList.YellowPositions.get(raw_position_id).Point;
            case 3 -> PositionList.BluePositions.get(raw_position_id).Point;
            case 4 -> PositionList.GreenPositions.get(raw_position_id).Point;
            default -> null;
        };
        drawPlane(this.g, point, getPlaneImage(player_id, raw_position_id), stack);
    }


    public final static HashMap<Integer, Point> HangerPoints = new HashMap<>() {{
        put(11, new Point(105, 505));
        put(12, new Point(105, 575));
        put(13, new Point(35, 575));
        put(14, new Point(35, 505));

        put(21, new Point(108, 105));
        put(22, new Point(35, 105));
        put(23, new Point(35, 35));
        put(24, new Point(108, 35));

        put(31, new Point(503, 109));
        put(32, new Point(577, 109));
        put(33, new Point(577, 35));
        put(34, new Point(503, 35));

        put(41, new Point(502, 503));
        put(42, new Point(575, 503));
        put(43, new Point(575, 575));
        put(44, new Point(502, 575));
    }};
}
