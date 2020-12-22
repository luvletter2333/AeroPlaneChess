package me.luvletter.planechess.model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class Battle {
    public List<Integer> stack1;
    public List<Integer> stack2;
    public int planeID1;
    public int planeID2;
    public int destPosition;
    private List<BattleResult> results;
    public List<Integer> remainstack;
    private Function<Void, Integer> onceDice;

    /**
     * stack1是Battle的发起者
     */
    public Battle(List<Integer> stack1, List<Integer> stack2, int destPosition, Function<Void, Integer> onceDice) {
        this.stack1 = stack1;
        this.stack2 = stack2;
        this.planeID1 = this.stack1.get(0);
        this.planeID2 = this.stack2.get(0);
        this.onceDice = onceDice;
        this.destPosition = destPosition;
        this.results = new ArrayList<>();
        this.remainstack = new ArrayList<>();
    }

    /**
     * 用于服务器在想客户端传递数据时的deepCopy
     */
    public Battle(Battle oldObj) {
        this.stack1 = new ArrayList<>(oldObj.stack1);
        this.stack2 = new ArrayList<>(oldObj.stack2);
        this.planeID1 = oldObj.planeID1;
        this.planeID2 = oldObj.planeID2;
        this.results = new ArrayList<>();
        for (BattleResult battleResult : oldObj.results) {
            this.results.add(new BattleResult(battleResult));
        }
        this.onceDice = null;
        this.remainstack = new ArrayList<>(oldObj.remainstack);
        this.destPosition = oldObj.destPosition;
    }

    public Battle() {
    }

    /**
     * 计算ballte结果并存入BattleResult
     *
     * @return 胜者的playerID
     */
    public int calculateResult() {
        var _stack1 = new ArrayList<>(this.stack1);
        var _stack2 = new ArrayList<>(this.stack2);

        while (_stack1.size() > 0 && _stack2.size() > 0) {
            int plane1 = _stack1.get(0);
            // list2还有飞机
            int plane2 = _stack2.get(0);
            int d1;
            int d2;
            do {
                d1 = onceDice();
                d2 = onceDice();
            } while (d1 == d2);
            results.add(new BattleResult(plane1, plane2, d1, d2));
            System.out.printf("[Battle Result] stack1:%s stack2:%s p1:%d p2:%d d1:%d d2:%d From calculateResult\n", _stack1, _stack2, plane1, plane2, d1, d2);
            System.out.println(this.results);
            if (d1 > d2) {
                _stack2.remove(Integer.valueOf(plane2));
            } else {
                _stack1.remove(Integer.valueOf(plane1));
            }
        }
        this.remainstack.addAll(_stack1);
        this.remainstack.addAll(_stack2);
        // _stack1 _stack2一定有一个空的
        return _stack1.size() > 0 ? _stack1.get(0) / 10 : _stack2.get(0) / 10;
    }

    private int onceDice() {
        return this.onceDice.apply(null);
    }

    public List<BattleResult> getResults() {
        return results;
    }

    public int getWinnerPlayerID() {
        return this.remainstack.get(0) / 10 == this.planeID1 / 10
                ? this.planeID1 / 10 : this.planeID2 / 10;
    }

    @Override
    public String toString() {
        return "Battle{" +
                "stack1=" + stack1 +
                ", stack2=" + stack2 +
                ", results=" + results +
                ", remainstack=" + remainstack +
                ", planeID1=" + planeID1 +
                ", planeID2=" + planeID2 +
                '}';
    }
}
