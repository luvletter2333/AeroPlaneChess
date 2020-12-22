package me.luvletter.planechess.client;

import me.luvletter.planechess.model.PlayerColor;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.stream.Collectors;

import static me.luvletter.planechess.client.PositionList.*;

public class ChessBoardClickHelper {

    public static Point getPointfromMouseEvent(MouseEvent me) {
        return new Point(me.getX(), me.getY());
    }

    private static double Sqdistance(Point A, Point B) {
        return java.awt.Point.distanceSq(A.X, A.Y, B.X, B.Y);
    }

    private static final Color redSample = new Color(240, 0, 0);
    private static final Color yellowSample = new Color(240, 240, 0);
    private static final Color blueSample = new Color(96, 111, 240);
    private static final Color greenSample = new Color(0, 159, 0);

    private static double colorDistance(int r1, int g1, int b1, Color c) {
        return (c.getRed() - r1) * (c.getRed() - r1)
                + (c.getGreen() - g1) * (c.getGreen() - g1)
                + (c.getBlue() - b1) * (c.getBlue() - b1);
    }

    private static PlayerColor matchColor(int RGB) {
        double tmp;
        int red = (RGB & 0x00ff0000) >> 16;
        int green = (RGB & 0x0000ff00) >> 8;
        int blue = RGB & 0x000000ff;
        tmp = colorDistance(red, green, blue, redSample);
        if (tmp < 100) return PlayerColor.Red;
        tmp = colorDistance(red, green, blue, yellowSample);
        if (tmp < 100) return PlayerColor.Yellow;
        tmp = colorDistance(red, green, blue, blueSample);
        if (tmp < 100) return PlayerColor.Blue;
        tmp = colorDistance(red, green, blue, greenSample);
        if (tmp < 100) return PlayerColor.Green;
        return null;
    }

    public static Position matchPositionfromPoint(Point p) {
        int click_RGB = Resource.getResource(ResourceType.ChessBoard).getRGB(p.X, p.Y);
        var mat = matchColor(click_RGB);
        Collection<Position> ps;
        if (mat != null)
            ps = switch (mat) {
                case Red -> RedPositions.values();
                case Green -> GreenPositions.values();
                case Yellow -> YellowPositions.values();
                case Blue -> BluePositions.values();
                default -> null;
            };
        else // click on White part
            ps = Positions;
        double min = 999999;
        Position minPosition = null;
        for (var en : ps) {
            var tmp = Sqdistance(p, en.Point);
            if (tmp < min) {
                min = tmp;
                minPosition = en;
            }
        }
        //System.out.println(min);
        if (mat != null)
            return minPosition; // Color based match, trusted result
        else if (min < 500)
            return minPosition; // Click on white, but still close
        else return null;
    }


    private static final ArrayList<Position> Positions = new ArrayList<>() {{
        addAll(all.values());
        addAll(DrawHelper.HangerPoints.entrySet().stream()
                .map(entry -> new Position(entry.getKey() / 10 * 100 + 99, entry.getValue().X, entry.getValue().Y))
                .collect(Collectors.toList()));
    }};
}
