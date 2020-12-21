package me.luvletter.planechess.util;

import me.luvletter.planechess.client.PositionList;
import me.luvletter.planechess.server.PlaneStack;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class Utility {
    public static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void setJPanelTitle(JPanel target, String title) {
        target.setBorder(BorderFactory.createTitledBorder(title));
    }

    /**
     * Calculate the first destination position id
     * in this method, jump or fly is not considered
     *
     * @return 104, 205, etc. -1 means out of bound.
     */
    public static int calculateDestPos(int player_id, int fromPos, int step) {
        if (fromPos % 100 >= 13) {
            int pos_id = fromPos % 100;
            if (pos_id + step > 19)
                return -1;
            return player_id * 100 + pos_id + step;
        }
        // we assume it still in the circle loop
        int start_index = PositionList.safeIndexOfCircleBoard(fromPos, player_id);
        int end_index = 0;
        for (int i = 0; i <= step; i++) {
            end_index = (start_index + i) % 52;
            // meet its entrance to final approach
            if (PositionList.circleBoard.get(end_index) == 13 + player_id * 100) {
                int last_step = step - i;
                if (last_step > 6)
                    return -1;
                else
                    return player_id * 100 + 13 + last_step;
            }
        }
        // not meet entrance
        return PositionList.all.get(PositionList.circleBoard.get(end_index)).ID;
    }

    /**
     * check whether given point is the Flying Point
     */
    public static boolean isFlyingPoint(int start_pos) {
        return start_pos % 10 == 5;
        // Flying Points:
        // 105, 205, 305, 405
    }

    /**
     * get the Jump Destination Position's ID
     * Please make sure there is a jump
     */
    public static int getJumpDestination(int start_pos) {
        if (isFlyingPoint(start_pos)) {
            return start_pos + 3;
        }
        // not flying point
        return start_pos + 1;
    }

    public static ArrayList<Integer> getStackedPlanes(List<PlaneStack> planeStacks, int planeID) {
        for (PlaneStack planeStack : planeStacks) {
            if (planeStack.hasPlane(planeID))
                return new ArrayList<>(planeStack.getStacked_planes());
        }
        return null;
    }

    public static ArrayList<Integer> getStackerPlanesOrGenerate(List<PlaneStack> planeStacks, int planeID) {
        ArrayList<Integer> ret = getStackedPlanes(planeStacks, planeID);
        if (ret == null) {
            ret = new ArrayList<>();
            ret.add(planeID);
        }
        return ret;
    }
}
