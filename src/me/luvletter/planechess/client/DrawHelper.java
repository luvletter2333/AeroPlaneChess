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

    private final BufferedImage ResultImage;
    private final Graphics g;
    private final HangerDrawHelper hangerDrawHelper;

    public DrawHelper(){
        this.ResultImage = Resource.copyImage(Resource.getResource(ResourceType.ChessBoard));
        g = this.ResultImage.getGraphics();
        this.hangerDrawHelper = new HangerDrawHelper();
    }

    public BufferedImage getResultImage() {
        return ResultImage;
    }

    public HangerDrawHelper getHangerDrawHelper() {
        return hangerDrawHelper;
    }

    public void Draw(int pos_id, int raw_position_id){
        // key -> 24 means the fourth plane of player 2
        final int player = pos_id / 10; // from 1 to 4
        final Point pos = (raw_position_id % 100 == 0) ? hangerDrawHelper.getPoint(raw_position_id)
                : switch (player) {
            case 1 -> PositionList.RedPositions.get(raw_position_id).Point;
            case 2 -> PositionList.YellowPositions.get(raw_position_id).Point;
            case 3 -> PositionList.BluePositions.get(raw_position_id).Point;
            case 4 -> PositionList.GreenPositions.get(raw_position_id).Point;
            default -> null;
        };
        drawPlane(g, pos, player);
    }
}
