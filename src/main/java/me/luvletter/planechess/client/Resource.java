package me.luvletter.planechess.client;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class Resource {

    // Cached Resource
    private static final HashMap<ResourceType, BufferedImage> resources = new HashMap<>();

    public static BufferedImage getResource(ResourceType type) {
        if (resources.size() == 0)
            loadResources();
        return resources.get(type);
    }

    /**
     * planeColor: 1,2,3,4 => Red, Yellow, Blue, Green
     *
     * @param planeColor for debug only
     */
    public static BufferedImage getPlaneImage(int planeColor) {
        return switch (planeColor) {
            case 1 -> Resource.getResource(ResourceType.Red_Plane);
            case 2 -> Resource.getResource(ResourceType.Yellow_Plane);
            case 3 -> Resource.getResource(ResourceType.Blue_Plane);
            case 4 -> Resource.getResource(ResourceType.Green_Plane);
            default -> null;
        };
    }

    public static BufferedImage getPlaneImage(int planeColor, int planePos) {
        if (planePos % 100 != 19)
            return getPlaneImage(planeColor);
        return switch (planeColor) {
            case 1 -> Resource.getResource(ResourceType.Win_Red_Plane);
            case 2 -> Resource.getResource(ResourceType.Win_Yellow_Plane);
            case 3 -> Resource.getResource(ResourceType.Win_Blue_Plane);
            case 4 -> Resource.getResource(ResourceType.Win_Green_Plane);
            default -> null;
        };
    }

    public static void loadResources() {
        var res = ResourceType.values();
        for (var r : res) {
            String path = getResourcePath(r);
            try {
                var img = ImageIO.read(new File(path));
                resources.put(r, img);
            } catch (IOException ioe) {
                System.out.printf("IOException when trying to load Resource %s from path %s\n", r, path);
                ioe.printStackTrace();
            }
        }
    }

    public static BufferedImage copyImage(BufferedImage source) {
        var b = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
        Graphics g = b.getGraphics();
        g.drawImage(source, 0, 0, null);
        g.dispose();
        return b;
    }

    private static String getResourcePath(ResourceType type) {
        return switch (type) {
            case ChessBoard -> "res/chessboard.png";
            case Red_Plane -> "res/red.png";
            case Yellow_Plane -> "res/yellow.png";
            case Blue_Plane -> "res/blue.png";
            case Green_Plane -> "res/green.png";
            case Win_Red_Plane -> "res/red_win.png";
            case Win_Yellow_Plane -> "res/yellow_win.png";
            case Win_Blue_Plane -> "res/blue_win.png";
            case Win_Green_Plane -> "res/green_win.png";
            case Dice_Unknown -> "res/dice/dice.png";
            case Dice_Rolling1 -> "res/dice/dice_roll1.png";
            case Dice_Rolling2 -> "res/dice/dice_roll2.png";
            case Dice_Rolling3 -> "res/dice/dice_roll3.png";
            case Dice_Rolling4 -> "res/dice/dice_roll4.png";
            case Dice_Rolling5 -> "res/dice/dice_roll5.png";
            case Dice_Rolling6 -> "res/dice/dice_roll6.png";
            case Dice_Rolling7 -> "res/dice/dice_roll7.png";
            case Dice_Rolling8 -> "res/dice/dice_roll8.png";
            case Dice_Rolled1 -> "res/dice/dice_rolled1.png";
            case Dice_Rolled2 -> "res/dice/dice_rolled2.png";
            case Dice_Rolled3 -> "res/dice/dice_rolled3.png";
            case Dice_Rolled4 -> "res/dice/dice_rolled4.png";
            case Dice_Rolled5 -> "res/dice/dice_rolled5.png";
            case Dice_Rolled6 -> "res/dice/dice_rolled6.png";
        };
    }
}
