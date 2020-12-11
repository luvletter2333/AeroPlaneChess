package me.luvletter.planechess.client;

import java.util.*;

public final class PositionList {
    /**
     * 1xx
     * */
    public static final Map<Integer, Position> RedPositions = Collections.unmodifiableMap(new HashMap<>(20) {{
        put(100, new Position(100, 169, 593));
        put(101, new Position(101, 180, 523));
        put(102, new Position(102, 126, 432));
        put(103, new Position(103, 34, 342));
        put(104, new Position(104, 49, 192));
        put(105, new Position(105, 192, 167));
        put(106, new Position(106, 233, 36));
        put(107, new Position(107, 377, 36));
        put(108, new Position(108, 420, 168));
        put(109, new Position(109, 560, 194));
        put(110, new Position(110, 575, 341));
        put(111, new Position(111, 484, 432));
        put(112, new Position(112, 431, 522));
        put(113, new Position(113, 305, 575));
        put(114, new Position(114, 305, 536));
        put(115, new Position(115, 305, 498));
        put(116, new Position(116, 305, 457));
        put(117, new Position(117, 305, 417));
        put(118, new Position(118, 305, 378));
        put(119, new Position(119, 305, 339));
    }});

    /**
     * 2xx
     * */
    public static final Map<Integer, Position> YellowPositions = Collections.unmodifiableMap(new HashMap<>(20) {{
        put(200, new Position(200, 22, 166));
        put(201, new Position(201, 89, 179));
        put(202, new Position(202, 178, 124));
        put(203, new Position(203, 269, 35));
        put(204, new Position(204, 417, 49));
        put(205, new Position(205, 445, 192));
        put(206, new Position(206, 574, 232));
        put(207, new Position(207, 575, 376));
        put(208, new Position(208, 445, 418));
        put(209, new Position(209, 417, 561));
        put(210, new Position(210, 268, 574));
        put(211, new Position(211, 178, 485));
        put(212, new Position(212, 89, 429));
        put(213, new Position(213, 32, 305));
        put(214, new Position(214, 75, 305));
        put(215, new Position(215, 114, 305));
        put(216, new Position(216, 151, 305));
        put(217, new Position(217, 192, 305));
        put(218, new Position(218, 233, 305));
        put(219, new Position(219, 273, 305));
    }});

    /**
     * 3xx
     * */
    public static final Map<Integer, Position> BluePositions = Collections.unmodifiableMap(new HashMap<>(20) {{
        put(300, new Position(300, 439, 25));
        put(301, new Position(301, 430, 89));
        put(302, new Position(302, 485, 178));
        put(303, new Position(303, 574, 269));
        put(304, new Position(304, 562, 419));
        put(305, new Position(305, 417, 444));
        put(306, new Position(306, 376, 574));
        put(307, new Position(307, 232, 575));
        put(308, new Position(308, 193, 448));
        put(309, new Position(309, 47, 418));
        put(310, new Position(310, 33, 268));
        put(311, new Position(311, 123, 177));
        put(312, new Position(312, 178, 87));
        put(313, new Position(313, 305, 35));
        put(314, new Position(314, 305, 75));
        put(315, new Position(315, 305, 113));
        put(316, new Position(316, 305, 153));
        put(317, new Position(317, 305, 191));
        put(318, new Position(318, 305, 231));
        put(319, new Position(319, 305, 268));
    }});

    /**
     * 4xx
     * */
    public static final Map<Integer, Position> GreenPositions = Collections.unmodifiableMap(new HashMap<>(20) {{
        put(400, new Position(400, 592, 444));
        put(401, new Position(401, 520, 430));
        put(402, new Position(402, 430, 485));
        put(403, new Position(403, 341, 576));
        put(404, new Position(404, 192, 561));
        put(405, new Position(405, 166, 419));
        put(406, new Position(406, 33, 377));
        put(407, new Position(407, 34, 232));
        put(408, new Position(408, 165, 193));
        put(409, new Position(409, 192, 47));
        put(410, new Position(410, 340, 33));
        put(411, new Position(411, 431, 124));
        put(412, new Position(412, 520, 178));
        put(413, new Position(413, 574, 305));
        put(414, new Position(414, 535, 305));
        put(415, new Position(415, 495, 305));
        put(416, new Position(416, 455, 305));
        put(417, new Position(417, 416, 305));
        put(418, new Position(418, 375, 305));
        put(419, new Position(419, 336, 305));
    }});

    public static final Map<Integer, Position> all = Collections.unmodifiableMap(new HashMap<>(80) {{
        putAll(RedPositions);
        putAll(YellowPositions);
        putAll(BluePositions);
        putAll(GreenPositions);
    }});

    /**
     * Positions in the order of circleBoard
     * */
    public static final List<Integer> circleBoard = Collections.unmodifiableList(new ArrayList<>(52) {{
        add(101);
        add(211);
        add(308);
        add(405);
        add(102);
        add(212);
        add(309);
        add(406);
        add(103);
        add(213);
        add(310);
        add(407);
        add(104);
        add(201);
        add(311);
        add(408);
        add(105);
        add(202);
        add(312);
        add(409);
        add(106);
        add(203);
        add(313);
        add(410);
        add(107);
        add(204);
        add(301);
        add(411);
        add(108);
        add(205);
        add(302);
        add(412);
        add(109);
        add(206);
        add(303);
        add(413);
        add(110);
        add(207);
        add(304);
        add(401);
        add(111);
        add(208);
        add(305);
        add(402);
        add(112);
        add(209);
        add(306);
        add(403);
        add(113);
        add(210);
        add(307);
        add(404);
    }});
}
