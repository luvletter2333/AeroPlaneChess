package me.luvletter.planechess.client;

import me.luvletter.planechess.Main;
import me.luvletter.planechess.server.PlaneStack;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

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

    private final BufferedImage ResultImage;
    private final Graphics g;

    public DrawHelper() {
        this.ResultImage = Resource.copyImage(Resource.getResource(ResourceType.ChessBoard));
        g = this.ResultImage.getGraphics();
    }

    public BufferedImage getResultImage() {
        return ResultImage;
    }

    public void Draw(int plane_id, int raw_position_id) {
        // key -> 24 means the fourth plane of player 2
        final int player_id = plane_id / 10; // from 1 to 4
        final Point point = (raw_position_id % 100 == 99) ? HangerPoints.get(plane_id)
                : switch (raw_position_id / 100) {
            case 1 -> PositionList.RedPositions.get(raw_position_id).Point;
            case 2 -> PositionList.YellowPositions.get(raw_position_id).Point;
            case 3 -> PositionList.BluePositions.get(raw_position_id).Point;
            case 4 -> PositionList.GreenPositions.get(raw_position_id).Point;
            default -> null;
        };
            drawPlane(g, point, getPlaneImage(player_id), plane_id);
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
