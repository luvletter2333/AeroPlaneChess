package me.luvletter.planechess.game;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import me.luvletter.planechess.Main;
import me.luvletter.planechess.model.PositionList;
import me.luvletter.planechess.event.EndThreadEvent;
import me.luvletter.planechess.event.Event;
import me.luvletter.planechess.event.EventManager;
import me.luvletter.planechess.event.gameevents.*;
import me.luvletter.planechess.game.client.AIClient;
import me.luvletter.planechess.game.client.DummyAIClient;
import me.luvletter.planechess.game.client.GameClient;
import me.luvletter.planechess.model.*;

import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

import static me.luvletter.planechess.util.Utility.*;

public class Game implements IGame {

    public int Player_Count;
    protected List<Integer> player_ids;
    /**
     * Key -> plane ID
     * Value -> Position ID, such as 102 302
     */
    private Map<Integer, Integer> planePosition;
    private List<PlaneStack> planeStacks;

    // Dice related:
    private int dice_player_id;
    private int dice_first_result;
    private int dice_second_result;
    private boolean dice_moved = false;
    private final Random dice_random;
    private Queue<Integer> cheatDice;

    private boolean has_won = false;
    private int win_player_id = 0;

    protected final Thread gameThread;
    protected final EventManager gameEventManager;

    protected volatile HashMap<Integer, GameClient> clients;

    private volatile Movement movement;
    private volatile HashSet<Integer> backPlane;


    public Game(int player_Count, ArrayList<Integer> player_ids) {
        this.Player_Count = player_Count;
        if (player_ids.size() != player_Count)
            throw new IllegalArgumentException("size of player_ids should equal to player_Count");
        this.player_ids = new ArrayList<>(player_ids);
        player_ids.sort(Integer::compareTo);
        this.planePosition = new HashMap<>();
        for (int id : player_ids) {
            planePosition.put(id * 10 + 1, id * 100 + 99);
            planePosition.put(id * 10 + 2, id * 100 + 99);
            planePosition.put(id * 10 + 3, id * 100 + 99);
            planePosition.put(id * 10 + 4, id * 100 + 99);
        }
        // initializePlanePosition
        this.planeStacks = new ArrayList<>();
        this.dice_random = new SecureRandom();
        this.cheatDice = new LinkedList<Integer>();
        this.clients = new HashMap<>(4);

        this.gameEventManager = new EventManager();
        this.gameThread = new Thread(this::processThread);
        this.gameThread.start();
    }

    public Game() {
        this.dice_random = new SecureRandom();
        this.gameThread = new Thread(this::processThread);
        this.gameEventManager = new EventManager();
        this.clients = new HashMap<>();
    }

    public void processThread() {
        while (true) {
            try {
                Event e = gameEventManager.get();
                System.out.println("[Game Event]" + e);
                switch (e.getType()) {
                    case GameMove -> {
                        MoveEvent me = (MoveEvent) e;
                        _move(me.plane_id, me.step, me.go_stack);
                    }
                    case GameSkip -> {
                        _skip(((SkipEvent) e).playerID);
                    }
                    case GameTakeOff -> _takeOff(((TakeOffEvent) e).playerID);
                    case GameBattle -> {
                        BattleEvent be = (BattleEvent) e;
                        _battle(be.planeID, be.step);
                    }
                    case GameAnnounceStart -> _announceStart();
                    case EndThreadEvent -> {
                        return;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * add Client, ~~and automatically bind Game~~
     */
    public void addClient(GameClient client) {
        if (!player_ids.contains(client.player_id))
            return;
        this.clients.put(client.player_id, client);
        //client.bindGame(this);
    }

    public void announceStart() {
        this.gameEventManager.push(new AnnounceStartEvent());
    }

    @Override
    public void takeOff(int player_id) {
        this.gameEventManager.push(new TakeOffEvent(player_id));
    }

    @Override
    public void skip(int player_id) {
        this.gameEventManager.push(new SkipEvent(player_id));
    }

    @Override
    public void move(int plane_id, int step, boolean go_stack) {
        this.gameEventManager.push(new MoveEvent(plane_id, step, go_stack));
    }

    @Override
    public void battle(int planeID, int step) {
        this.gameEventManager.push(new BattleEvent(planeID, step));
    }

    private void _announceStart() {
        if (!canStart())
            return;
        var init_cbs = getChessboardStatus();
        clients.values().forEach(client -> client.UpdateClientChessBoard(init_cbs, null, null, false, true));
        int start_player = player_ids.stream().min(Integer::compareTo).get();
        int dice_result = rolling_Dice(start_player);
        clients.get(start_player).Dice(DiceType.Fly, 2, dice_result);
        clients.values().stream()
                .filter(c -> c.player_id != start_player)
                .forEach(c -> c.ShowOtherDiceResult(start_player, DiceType.Fly, 2, dice_result));
        System.out.println("announce Start");
        // TODO: DEBUG
    }

    private boolean _takeOff(int player_id) {
        if (this.dice_player_id != player_id) // not your turn!!
            return false;
        if (this.dice_moved)     // you have moved!!
            return false;
        if (this.dice_first_result != 6 && this.dice_second_result != 6)
            return false;
        for (Map.Entry<Integer, Integer> entry : this.planePosition.entrySet()) {
            if (entry.getKey() / 10 == player_id)
                if (entry.getValue() % 100 == 99) {
                    // clear to take off
                    movePlane(entry.getKey(), player_id * 100);
                    this.backPlane = new HashSet<>();
                    this.movement = new Movement(entry.getKey(), (entry.getKey() / 10) * 100 + 99, player_id * 100);
                    updateClients();
                    this.dice_moved = true;
                    nextLoop();
                    return true;
                }
        }
        return false;

    }

    /**
     * You have nothing to do but skip this loop
     */
    private boolean _skip(int player_id) {
        if (this.dice_player_id != player_id)
            return false;
        this.dice_moved = true;
        this.movement = new Movement(0, 0, 0);
        this.backPlane = new HashSet<>();
        this.clients.values().stream().filter(client -> client.player_id != player_id)
                .forEach(client -> client.AnnounceOtherSkip(player_id));
        nextLoop();
        return true;
    }

    /**
     * Move a plane with given id `step` steps
     * Please check whether data are valid in server side
     *
     * @return false -> no cheat! or battle needed, true -> accepted
     */
    private boolean _move(int plane_id, int step, boolean go_stack) {
        if (!antiCheat(plane_id, step))
            return false;   // anti-cheat test not passed
        // validate step done!

        int start_pos = this.planePosition.get(plane_id);
        int start_index = PositionList.circleBoard.indexOf(start_pos);

        int end_index = (start_index + step) % 52;
        int end_pos = calculateDestPos(plane_id / 10, start_pos, step);

        // judge whether there will be a battle
        // the end position has planes with **different** color
        // in jump mode, planes are sent to their home directly, no battle needed
        if (this.planePosition.entrySet().stream()
                .anyMatch(entry -> entry.getValue() == end_pos && (entry.getKey() / 10) != (plane_id / 10)))
            return false;

        baseMove(plane_id, step, go_stack);
        updateClients();
        this.dice_moved = true;
        nextLoop();
        return true;

    }

    private boolean _battle(int planeID, int step) {
        if (!antiCheat(planeID, step))
            return false;
        int playerID = planeID / 10;
        int destPos = calculateDestPos(playerID, this.planePosition.get(planeID), step);
        // get stack at destPos,被battle的stack
        var stack2 = new ArrayList<Integer>();
        for (int i : this.planePosition.keySet()) {
            if (i / 10 == playerID) continue;
            if (this.planePosition.get(i) == destPos)
                stack2.add(i);
        }
        if (stack2.size() == 0)
            return false;
        System.out.println("被battle的stack" + stack2);
        int rep2_pid = stack2.get(0); // 被battle stack的一个代表
        var stack1 = getStackerPlanesOrGenerate(this.planeStacks, planeID);
        var battle = new Battle(stack1, stack2, destPos, unused -> onceDice());
        int winner_playerID = battle.calculateResult();
        // Apply Battle Result
        if (winner_playerID == playerID) {
            // playerID win this battle
            applyBattleResult(stack1, battle.remainstack, destPos, stack2);
        } else {
            // playerID lose this battle
            applyBattleResult(stack2, battle.remainstack, destPos, stack1);
        }
        clients.values().forEach(client -> client.AnnounceBattleResult(getChessboardStatus(), battle));
        this.movement = null;
        this.backPlane = null;
        this.dice_moved = true;
        nextLoop();
        return true;
    }

    private void applyBattleResult(List<Integer> winner_stack,
                                   List<Integer> winner_remain_stack, int destPos,
                                   List<Integer> loser_stack) {
        int winner_rep = winner_remain_stack.get(0);
        int loser_rep = loser_stack.get(0);
        // send back loser
        loser_stack.forEach(loser_pid -> this.planePosition.put(loser_pid, loser_rep / 10 * 100 + 99));
        // unstack loser
        this.planeStacks.removeIf(planeStack -> planeStack.hasPlane(loser_rep));

        // 胜方(winner_stack)可能仍然有棋子要回家，计算差集 ( winner_stack > winner_remain_stack)
        // 在stack1中 但是不再remain stack中
        var winner_back = winner_stack.stream()
                .filter(pid -> !winner_remain_stack.contains(pid)).collect(Collectors.toList());

        // 删除现有的planeStack，添加新的
        this.planeStacks.removeIf(planeStack -> planeStack.hasPlane(winner_rep));
        if (winner_remain_stack.size() > 1)
            this.planeStacks.add(new PlaneStack(new HashSet<>(winner_remain_stack)));

        winner_back.forEach(back_pid -> this.planePosition.put(back_pid, back_pid / 10 * 100 + 99));
        winner_remain_stack.forEach(remain_pid -> this.planePosition.put(remain_pid, destPos));
    }

    protected boolean canStart() {
        return this.clients.size() == this.Player_Count;
    }

    /**
     * Rolling Dice in Dice Type Fly, return an integer number in range [1,60]
     *
     * @return result / 10 -> first. result % 10 -> second
     */
    private int rolling_Dice(int player_id) {
        this.dice_player_id = player_id;
        this.dice_moved = false;
        this.dice_first_result = onceDice();
        this.dice_second_result = onceDice();
        return this.dice_first_result * 10 + this.dice_second_result;
    }

    private int onceDice() {
        if (this.cheatDice.size() == 0)
            return this.dice_random.nextInt(6) + 1;
        else {
            var ret = this.cheatDice.poll();
            System.out.println("[Game] Dice Cheated: " + ret);
            return ret;
        }
    }

    /**
     * @return true -> test passed
     */
    private boolean antiCheat(int plane_id, int step) {
        if (step <= 0)
            return false;
        if (step > 12)
            return false;
        if (this.dice_player_id != plane_id / 10) // not your turn!!
            return false;
        if (this.dice_moved)     // you have moved!!
            return false;
        if (this.planePosition.get(plane_id) % 100 == 99)
            return false;       // you are in the hanger!
        // Anti-Cheat done!

        var possibleMove = new ArrayList<Integer>();
        possibleMove.add(dice_first_result + dice_second_result);
        possibleMove.add(dice_first_result * dice_second_result);
        possibleMove.add(Math.abs(dice_first_result - dice_second_result));
        if (dice_first_result % dice_second_result == 0)
            possibleMove.add(dice_first_result / dice_second_result);
        if (dice_second_result % dice_first_result == 0)
            possibleMove.add(dice_second_result / dice_first_result);
        if (possibleMove.stream().noneMatch(mv -> mv == step))
            return false;

        if (calculateDestPos(plane_id / 10, this.planePosition.get(plane_id), step) == -1)
            return false;       // out of bound

        return true;
    }

    /**
     * in baseMove, we don't consider there is any battle.
     * Guaranteed by battle judgement
     * backPlane and movement will be initialized and set
     */
    private void baseMove(int plane_id, int step, boolean go_stack) {
        this.backPlane = new HashSet<>();
        // check whether plane is in a stack
        var in_stacks = planeStacks.stream()
                .filter(stack -> stack.hasPlane(plane_id)).collect(Collectors.toList());
        PlaneStack stack = null;
        if (in_stacks.size() != 0)
            stack = in_stacks.get(0); // must have one;
        // stack == null -> not in any stack

        int start_pos = this.planePosition.get(plane_id);
        int end_pos = calculateDestPos(plane_id / 10, start_pos, step);
        // get initial landing position

        int final_end_pos = end_pos;

        if (end_pos / 100 == plane_id / 10 && end_pos % 100 < 13) {
            // the same color -> ready to jump (first jump)
            // whether there are some planes in the first jump destination position
            tryBackPlanes(plane_id, end_pos, start_pos);
            // if the first jump is a fly, then it leads a double jump
            int middle_pos = getJumpDestination(end_pos);
            if (isFlyingPoint(end_pos)) {
                final_end_pos = getJumpDestination(middle_pos);
                tryBackPlanes(plane_id, middle_pos, end_pos);
                tryBackPlanes(plane_id, final_end_pos, middle_pos);
                if (go_stack) {
                    tryStackPlanes(plane_id, end_pos);
                    tryStackPlanes(plane_id, middle_pos);
                    tryStackPlanes(plane_id, final_end_pos);
                }
                movePlane(plane_id, final_end_pos);
                this.movement = new Movement(plane_id, start_pos, final_end_pos).addKeyPoint(end_pos).addKeyPoint(middle_pos);
                return;
            } else { // the first jump is a simple jump
                if (isFlyingPoint(middle_pos)) { //the second is a flying jump
                    final_end_pos = getJumpDestination(middle_pos);
                    tryBackPlanes(plane_id, middle_pos, end_pos);
                    tryBackPlanes(plane_id, final_end_pos, middle_pos);
                    if (go_stack) {
                        tryStackPlanes(plane_id, end_pos);
                        tryStackPlanes(plane_id, middle_pos);
                        tryStackPlanes(plane_id, final_end_pos);
                    }
                    movePlane(plane_id, final_end_pos);
                    this.movement = new Movement(plane_id, start_pos, final_end_pos).addKeyPoint(end_pos).addKeyPoint(middle_pos);
                } else { // the first jump is a simple jump, and no second jump
                    final_end_pos = middle_pos;
                    tryBackPlanes(plane_id, final_end_pos, end_pos);
                    if (go_stack) {
                        tryStackPlanes(plane_id, end_pos);
                        tryStackPlanes(plane_id, final_end_pos);
                    }
                    movePlane(plane_id, final_end_pos);
                    this.movement = new Movement(plane_id, start_pos, final_end_pos).addKeyPoint(end_pos);
                }
            }
        } else { // not jump, just move (there won't be any battle needed, promised by antiCheat)
            movePlane(plane_id, final_end_pos);
            this.movement = new Movement(plane_id, start_pos, final_end_pos);
            if (go_stack)
                tryStackPlanes(plane_id, final_end_pos);
        }
        // check win
        if (checkWin(plane_id / 10)) {
            this.has_won = true;
            this.win_player_id = plane_id / 10;
        }
    }


    /**
     * Move Plane(s) at the original Pos of plane_id to destPos
     * Stack is automatically unstacked if destPos is x19, which means reach final
     */
    private void movePlane(int plane_id, int destPos) {
        if (this.planeStacks.stream().noneMatch(planeStack -> planeStack.hasPlane(plane_id))) {
            // the plane isn't in any stack, just update position_id
            this.planePosition.put(plane_id, destPos);
        } else {
            for (PlaneStack planeStack : this.planeStacks) {
                if (planeStack.hasPlane(plane_id)) {
                    planeStack.getStacked_planes().forEach(
                            stacked_plane_id -> this.planePosition.put(stacked_plane_id, destPos));
                    break;
                }
            }
            // unstack if win
            var iterator = this.planeStacks.iterator();
            while (iterator.hasNext()) {
                var planeStack = iterator.next();
                if (planeStack.hasPlane(plane_id)) { // not to the end point
                    if (destPos % 100 == 19) {
//                        planeStack.getStacked_planes().forEach(
//                                stacked_plane_id -> this.planePosition.put(stacked_plane_id, destPos / 100 + 19));
                        iterator.remove(); // unstack
                    } else {
                        planeStack.getStacked_planes().forEach(
                                stacked_plane_id -> this.planePosition.put(stacked_plane_id, destPos));
                    }
                    break;
                }
            }
        }
    }

    /**
     * try Send Back Planes without battle
     * if they should be sent back, they are unstacked
     */
    private void tryBackPlanes(int source_plane_id, int dest_pos, int source_pos) {
        var diedPlanes = new ArrayList<Integer>(); // plane's ID which is going to die
        this.planePosition.forEach((pid, ppos) -> {
            if (ppos.equals(dest_pos) && pid / 10 != source_plane_id / 10) // not in the same color
                diedPlanes.add(pid);
        });

        // flying across the final approach line
        if (source_pos != 0 && isFlyingPoint(source_pos)) {
            switch (source_plane_id / 10) {
                case 1 -> tryBackPlanes(source_plane_id, 316, 0);
                case 2 -> tryBackPlanes(source_plane_id, 216, 0);
                case 3 -> tryBackPlanes(source_plane_id, 116, 0);
                case 4 -> tryBackPlanes(source_plane_id, 416, 0);
            }
        }
        for (Integer diedPlane_id : diedPlanes) {
            movePlane(diedPlane_id, (diedPlane_id / 10) * 100 + 99);
        }
        // un-stack
        if (diedPlanes.size() > 0) {
            this.planeStacks.removeIf(planeStack -> planeStack.hasPlane(diedPlanes.get(0)));
        }
        // add them into backPlanes
        this.backPlane.addAll(diedPlanes);
        if (Main.DEBUG_MODE)
            System.out.println("Removed Planes:" + diedPlanes.toString());
    }

    /**
     * try to combine plane in given position into a stack
     *
     * @return true -> success, false -> not satisfied planes
     */
    private boolean tryStackPlanes(int plane_id, int position_id) {
        if (position_id % 100 == 19)
            return false;
        for (PlaneStack planeStack : this.planeStacks) {
            if (this.planePosition.get(planeStack.getStacked_planes().toArray()[0]) == position_id) { // there is a stack in given position
                if (!planeStack.getStacked_planes().contains(plane_id))
                    planeStack.addPlane(plane_id);
                return true;
            }
            if (planeStack.getStacked_planes().contains(plane_id)) {
                this.planePosition.forEach((pid, pps) -> {
                    if (pid / 10 == plane_id / 10 && pps == position_id) {
                        // create a stack
                        planeStack.addPlane(pid);
                    }
                });
                return true;
            }
        }
        // there isn't any existing stack
        var stack = new PlaneStack(plane_id);
        this.planePosition.forEach((pid, pps) -> {
            if (pid / 10 == plane_id / 10 && pps == position_id) {
                // create a stack
                stack.addPlane(pid);
            }
        });
        if (stack.planeCount() > 1) {
            this.planeStacks.add(stack);
            return true;
        }
        return false;
    }

    /**
     * check whether `player_id` has won this game
     *
     * @param player_id 1, 2, 3, 4
     */
    private boolean checkWin(int player_id) {
        return this.planePosition.entrySet().stream()
                .filter(entry -> entry.getKey() / 10 == player_id)
                .allMatch(entry -> entry.getValue() % 100 == 19);
    }

    private ChessBoardStatus getChessboardStatus() {
        return new ChessBoardStatus(this.Player_Count, this.planePosition, this.planeStacks, has_won, win_player_id);
    }


    private void updateClients() {
        var cbs = getChessboardStatus();
        clients.values().forEach(c -> c.UpdateClientChessBoard(cbs, movement, backPlane, false, false));
        // TODO: Remove isSkipped
        if (this.has_won) {
            this.clients.values().forEach(c -> c.AnnounceWin(this.win_player_id));
            this.gameEventManager.clearEvents();
            this.gameEventManager.push(new EndThreadEvent());
        }
    }

    private void nextLoop() {
        if (has_won)
            return;
        int next_player = this.player_ids.get((this.player_ids.indexOf(this.dice_player_id) + 1) % this.Player_Count);
        System.out.println("[Game] next Player: " + next_player);
        int dice_result = rolling_Dice(next_player);
        clients.get(next_player).Dice(DiceType.Fly, 2, dice_result);
        clients.values().stream()
                .filter(c -> c.player_id != next_player)
                .forEach(c -> c.ShowOtherDiceResult(next_player, DiceType.Fly, 2, dice_result));
    }

    @Override
    public String saveGame() {
        JSONObject ret = new JSONObject();
        ret.put("Player_Count", this.Player_Count);
        ret.put("player_ids", this.player_ids);
        ret.put("planePosition", this.planePosition);
        ret.put("planeStacks", this.planeStacks);

        ret.put("dice_player_id", this.dice_player_id);
        ret.put("dice_first_result", this.dice_first_result);
        ret.put("dice_second_result", this.dice_second_result);
        ret.put("dice_moved", this.dice_moved);
        ret.put("cheatDice", this.cheatDice);

        ret.put("AIClients", clients.entrySet().stream()
                .filter(entry -> entry.getValue() instanceof AIClient)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList()));
        ret.put("DummyAIClients", clients.entrySet().stream()
                .filter(entry -> entry.getValue() instanceof DummyAIClient)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList()));

        return ret.toJSONString();
    }

    @Override
    public boolean loadGame(String json) {
        try {
            JSONObject obj = JSON.parseObject(json);
            this.Player_Count = obj.getIntValue("Player_Count");
            this.player_ids = obj.getJSONArray("player_ids").toJavaList(Integer.class);
            this.planePosition = (Map<Integer, Integer>) obj.getJSONObject("planePosition").toJavaObject(Map.class);
            this.planeStacks = obj.getJSONArray("planeStacks").toJavaList(PlaneStack.class);
            this.dice_player_id = obj.getIntValue("dice_player_id");
            this.dice_first_result = obj.getIntValue("dice_first_result");
            this.dice_second_result = obj.getIntValue("dice_second_result");
            this.dice_moved = obj.getBooleanValue("dice_moved");
            this.cheatDice = new ArrayDeque<>(obj.getJSONArray("dice_first_result").toJavaList(Integer.class));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Only for debug
     */
    public void testforceMove(int plane_id, int step, boolean go_stack) {
        if (Main.DEBUG_MODE) {
            baseMove(plane_id, step, go_stack);
            updateClients();
            this.dice_moved = true;
        }
    }

    public void testforceMoveTo(int plane_id, int dest) {
        if (!Main.DEBUG_MODE)
            return;
        int start_pos = this.planePosition.get(plane_id);
        movePlane(plane_id, dest);
        this.backPlane = new HashSet<>();
        this.movement = new Movement(plane_id, start_pos, dest);
        updateClients();
    }

    public void testforceModifyDiceResult(int dice_first_result, int dice_second_result) {
        if (!Main.DEBUG_MODE)
            return;
        this.dice_first_result = dice_first_result;
        this.dice_second_result = dice_second_result;
    }

    public void testStatus() {
        if (!Main.DEBUG_MODE)
            return;
        System.out.println(this.planePosition);
        System.out.println(this.planeStacks);
        System.out.println(String.format("Dice: owner:%s 1st:%s 2nd:%s moved:%s", this.dice_player_id, this.dice_first_result, this.dice_second_result, this.dice_moved));
    }

    public void testNextLoop() {
        if (!Main.DEBUG_MODE)
            return;
        this.nextLoop();
    }

    public void testSkip(int player_id) {
        if (!Main.DEBUG_MODE)
            return;
        this._skip(player_id);
    }

    public void testReStart() {
        this.planePosition = new HashMap<>();
        for (int id : player_ids) {
            planePosition.put(id * 10 + 1, id * 100 + 99);
            planePosition.put(id * 10 + 2, id * 100 + 99);
            planePosition.put(id * 10 + 3, id * 100 + 99);
            planePosition.put(id * 10 + 4, id * 100 + 99);
        }
        // initializePlanePosition
        this.planeStacks = new ArrayList<>();
        this.cheatDice = new ArrayDeque<>();
        _announceStart();
    }

    public void testCheatDice(Queue<Integer> cheatDice) {
        this.cheatDice.addAll(cheatDice);
    }

    public void testUpdateUI() {
        this.updateClients();
    }

}
