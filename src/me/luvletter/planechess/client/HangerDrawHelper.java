package me.luvletter.planechess.client;

import java.util.HashMap;
import java.util.Hashtable;

public class HangerDrawHelper {
    private HashMap<Integer, Counter> helper = new HashMap<>();

    public HangerDrawHelper() {
        helper.put(1, new Counter());
        helper.put(2, new Counter());
        helper.put(3, new Counter());
        helper.put(4, new Counter());
    }

    /**
     * pos -> 100, 200, 300, 400
     * position is automatically calculated
     * */
    public Point getPoint(int pos) {
        int id = pos / 100;
        int cnt = helper.get(id).increase();
        int map_id = id * 10 + cnt;
        return HangerPosition.get(map_id);
    }

    public final static HashMap<Integer, Point> HangerPosition = new HashMap<>() {{
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
