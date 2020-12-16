package me.luvletter.planechess.server;

import me.luvletter.planechess.Main;
import me.luvletter.planechess.client.Position;
import me.luvletter.planechess.client.PositionList;
import me.luvletter.planechess.event.EventManager;

import java.lang.reflect.Array;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

public class Game {

    public final int Player_Count;
    private final ArrayList<Integer> player_ids;
    /**
     * Key -> plane ID
     * Value -> Position ID, such as 102 302
     */
    private final HashMap<Integer, Integer> planePosition;
    private final ArrayList<PlaneStack> planeStacks;

    // Dice related:
    private int dice_player_id;
    private int dice_first_result;
    private int dice_second_result;
    private boolean dice_moved = false;
    private final Random dice_random;

    private boolean has_won = false;
    private int win_player_id = 0;

    private final Object lock_obj = new Object();

    private final HashMap<Integer, Client> clients;

    private volatile Movement movement;
    private volatile HashSet<Integer> backPlane;


    public Game(int player_Count, ArrayList<Integer> player_ids) {
        this.Player_Count = player_Count;
        if (player_ids.size() != player_Count)
            throw new IllegalArgumentException("size of player_ids should equal to player_Count");
        this.player_ids = new ArrayList<>(player_ids);
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
        this.clients = new HashMap<>(4);
    }

    /**
     * add Client, and automatically bind Game
     */
    public void addClient(Client client) {
        if (!player_ids.contains(client.player_id))
            return;
        this.clients.put(client.player_id, client);
        client.bindGame(this);
    }

    public void announceStart() {
        synchronized (lock_obj) {
            var init_cbs = getChessboardStatus();
            clients.values().forEach(client -> client.UpdateClientChessBoard(init_cbs, null, null, false, true));
            int start_player = player_ids.stream().min(Integer::compareTo).get();
            int dice_result = rolling_Dice(start_player);
            clients.get(start_player).Dice(DiceType.Fly, 2, dice_result);
            clients.values().stream()
                    .filter(c -> c.player_id != start_player)
                    .forEach(c -> c.ShowOtherDiceResult(start_player, DiceType.Fly, 2, dice_result));
        }
    }

    public boolean takeOff(int player_id) {
        synchronized (lock_obj) {
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
    }

    /**
     * You have nothing to do but skip this loop
     */
    public boolean skip(int player_id) {
        synchronized (lock_obj) {
            if (this.dice_player_id != player_id)
                return false;
            this.dice_moved = true;
            nextLoop();
            this.movement = null;
            this.backPlane = new HashSet<>();
            updateClients(true);
            return true;
        }
    }

    /**
     * Move a plane with given id `step` steps
     * Please check whether data are valid in server side
     *
     * @return false -> no cheat! or battle needed, true -> accepted
     */
    public boolean move(int plane_id, int step, boolean go_stack) {
        synchronized (lock_obj) {
            if (!antiCheat(plane_id, step))
                return false;   // anti-cheat test not passed
            // validate step done!

            int start_pos = this.planePosition.get(plane_id);
            int start_index = PositionList.circleBoard.indexOf(start_pos);

            int end_index = (start_index + step) % 52;
            int end_pos = PositionList.all.get(PositionList.circleBoard.get(end_index)).ID;

            // judge whether there will be a battle
            // the end position has planes with **different** color
            // in jump mode, planes are sent to their home directly, no battle needed
            if (this.planePosition.containsValue(end_pos)
                    && (end_pos / 100) != (plane_id / 10))
                return false;

            baseMove(plane_id, step, go_stack);
            updateClients();
            this.dice_moved = true;
            nextLoop();
            return true;
        }
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
        return this.dice_random.nextInt(6) + 1;
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

        if (this.calculateDestPos(plane_id / 10, this.planePosition.get(plane_id), step) == -1)
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

        if (end_pos / 100 == plane_id / 10 && end_pos % 100 <= 13) {  // the same color -> ready to jump (first jump)
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
        if (checkWin(plane_id / 10))
            declareWin(plane_id / 10);
    }

    /**
     * Calculate the destination position id
     *
     * @return 104, 205, etc. -1 means out of bound.
     */
    private int calculateDestPos(int player_id, int from, int step) {
        if (from % 100 >= 13) {
            int pos_id = from % 100;
            if (pos_id + step > 19)
                return -1;
            return player_id * 100 + pos_id + step;
        }
        // we assume it still in the circle loop
        int start_index = PositionList.circleBoard.indexOf(from);
        if (start_index == -1) {
            // fix starting point
            start_index = switch (player_id) {
                case 1 -> PositionList.circleBoard.indexOf(307);
                case 2 -> PositionList.circleBoard.indexOf(407);
                case 3 -> PositionList.circleBoard.indexOf(107);
                case 4 -> PositionList.circleBoard.indexOf(207);
                default -> 0;
            };
        }
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
    private boolean isFlyingPoint(int start_pos) {
        return start_pos % 10 == 5;
        // Flying Points:
        // 105, 205, 305, 405
    }

    /**
     * get the Jump Destination Position's ID
     * Please make sure there is a jump
     */
    private int getJumpDestination(int start_pos) {
        if (isFlyingPoint(start_pos)) {
            return start_pos + 3;
        }
        // not flying point
        return start_pos + 1;
    }

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
        }
        System.out.println("plane moved:" + this.planePosition.toString());
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
            boolean un_stack = false;
            PlaneStack stack_removed = null;
            for (PlaneStack planeStack : this.planeStacks) {
                if (planeStack.hasPlane(diedPlanes.get(0))) {
                    stack_removed = planeStack;
                    un_stack = true;
                    break;
                }
            }
            if (un_stack)
                this.planeStacks.remove(stack_removed);
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
        for (PlaneStack planeStack : this.planeStacks) {
            if (this.planePosition.get(planeStack.getStacked_planes().toArray()[0]) == position_id) { // there is a stack in given position
                if (!planeStack.getStacked_planes().contains(plane_id))
                    planeStack.addPlane(plane_id);
                return true;
            }
        }
        // there isn't any existing stack
        var stack = new PlaneStack().addPlane(plane_id);
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
        // TODO: assign position 198 to planes which arrive the final
        return this.planePosition.entrySet().stream()
                .filter(entry -> entry.getKey() / 10 == player_id)
                .allMatch(entry -> entry.getValue() % 100 == 19);
    }

    private void declareWin(int player_id) {
        this.has_won = true;
        this.win_player_id = player_id;
        clients.values().forEach(c -> c.declareWin(player_id));
    }

    private ChessBoardStatus getChessboardStatus() {
        return new ChessBoardStatus(this.Player_Count, this.planePosition, this.planeStacks, has_won, win_player_id);
    }

    private void updateClients() {
        updateClients(false);
    }

    private void updateClients(boolean isSkipped) {
        var cbs = getChessboardStatus();
        clients.values().forEach(c -> c.UpdateClientChessBoard(cbs, movement, backPlane, isSkipped, false));
    }

    private void nextLoop() {
        int next_player = this.player_ids.get((this.player_ids.indexOf(this.dice_player_id) + 1) % this.Player_Count);
        System.out.println("[Server] next Player: " + next_player);
        int dice_result = rolling_Dice(next_player);
        clients.get(next_player).Dice(DiceType.Fly, 2, dice_result);
        clients.values().stream()
                .filter(c -> c.player_id != next_player)
                .forEach(c -> c.ShowOtherDiceResult(next_player, DiceType.Fly, 2, dice_result));
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
        this.skip(player_id);
    }

}
