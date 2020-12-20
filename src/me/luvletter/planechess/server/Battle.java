package me.luvletter.planechess.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Battle {
    public final List<Integer> stack1;
    public final List<Integer> stack2;
    public final int planeID1;
    public final int planeID2;
    private final List<BattleResult> results;
    private final Random random;
    public final List<Integer> remainstack;

    /**
     * stack1是Battle的发起者
     */
    public Battle(List<Integer> stack1, List<Integer> stack2, Random random) {
        this.stack1 = stack1;
        this.stack2 = stack2;
        this.planeID1 = this.stack1.get(0);
        this.planeID2 = this.stack2.get(0);
        this.random = random;
        this.results = new ArrayList<>();
        this.remainstack = new ArrayList<>();
    }

    public Battle(Battle oldObj) {
        this.stack1 = new ArrayList<>( oldObj.stack1);
        this.stack2 = new ArrayList<>(oldObj.stack2);
        this.planeID1 = oldObj.planeID1;
        this.planeID2 = oldObj.planeID2;
        this.results = new ArrayList<>();
        for (BattleResult battleResult : oldObj.results) {
            this.results.add(new BattleResult(battleResult));
        }
        this.random = null;
        this.remainstack = new ArrayList<>(oldObj.remainstack);
    }

    /**
     * 计算ballte结果并存入BattleResult
     *
     * @return 胜者的playerID
     */
    public int calculateResult() {
        var _stack1 = new ArrayList<>(this.stack1);
        var _stack2 = new ArrayList<>(this.stack2);

        var iterator1 = _stack1.iterator();
        var iterator2 = _stack2.iterator();
        while (iterator1.hasNext()) {
            int plane1 = iterator1.next();
            if (!iterator2.hasNext())
                break;
            // list2还有飞机
            int plane2 = iterator2.next();
            int d1;
            int d2;
            do {
                d1 = onceDice();
                d2 = onceDice();
            } while (d1 == d2);
            results.add(new BattleResult(plane1, plane2, d1, d2));
            System.out.printf("stack1:%s stack2:%s p1:%d p2:%d d1:%d d2:%d\n", _stack1, _stack2, plane1, plane2, d1, d2);
            System.out.println(this.results);
            if (d1 < d2)
                iterator1.remove();
            else
                iterator2.remove();
        }
        this.remainstack.addAll(_stack1);
        this.remainstack.addAll(_stack2);
        // _stack1 _stack2一定又一个空的
        return _stack1.size() > 0 ? _stack1.get(0) / 10 : _stack2.get(0) / 10;
    }

    private int onceDice() {
        return this.random.nextInt(6) + 1;
    }

    public List<BattleResult> getResults() {
        return results;
    }


}
