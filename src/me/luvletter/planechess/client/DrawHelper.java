package me.luvletter.planechess.client;

import java.awt.*;
import java.awt.image.BufferedImage;

public class DrawHelper {
    /**
     * planeColor: 1,2,3,4 => Red, Yellow, Blue, Green
     * */
    public static void drawPlane(Graphics g, Point phyPoint, int planeColor){
        BufferedImage plane_img = switch (planeColor) {
            case 1 -> Resource.getResource(ResourceType.Red_Plane);
            case 2 -> Resource.getResource(ResourceType.Yellow_Plane);
            case 3 -> Resource.getResource(ResourceType.Blue_Plane);
            case 4 -> Resource.getResource(ResourceType.Green_Plane);
            default -> null;
        };
        g.drawImage(plane_img, phyPoint.X - 10, phyPoint.Y - 10, 20, 20, null);
        // scale 30x30 -> 20x20
    }

}
