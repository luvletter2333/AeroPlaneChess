package me.luvletter.planechess.client;

import java.util.HashMap;
import java.util.Hashtable;

public class HangerDrawHelper implements Cloneable {
    private final HashMap<Integer, Counter> helper;

    public HangerDrawHelper() {
        helper = new HashMap<>();
        helper.put(1, new Counter());
        helper.put(2, new Counter());
        helper.put(3, new Counter());
        helper.put(4, new Counter());
    }

    private HangerDrawHelper(HashMap<Integer, Counter> map){
        this.helper = map;
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

    @Override
    public HangerDrawHelper clone() throws CloneNotSupportedException {
        HangerDrawHelper clone = (HangerDrawHelper) super.clone();
        var newHashMap = new HashMap<Integer, Counter>();
        newHashMap.put(1, this.helper.get(1).clone());
        newHashMap.put(2, this.helper.get(2).clone());
        newHashMap.put(3, this.helper.get(3).clone());
        newHashMap.put(4, this.helper.get(4).clone());
        return new HangerDrawHelper(newHashMap);
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

class Counter implements Cloneable {
    private int value = 0;

    public Counter(){

    }

    private Counter(int value){
        this.value = value;
    }

    /**
     * from 1
     * */
    public int increase(){
        value++;
        return value;
    }

    public int getValue() {
        return value;
    }

    public void clear(){
        value = 0;
    }

    @Override
    public Counter clone() throws CloneNotSupportedException {
        Counter clone = (Counter) super.clone();
        return new Counter(this.value);
    }
}

