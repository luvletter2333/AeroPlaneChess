package me.luvletter.planechess.server;

import me.luvletter.planechess.Main;
import me.luvletter.planechess.client.Position;
import me.luvletter.planechess.client.PositionList;
import me.luvletter.planechess.event.EventManager;

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
    private volatile ServerMovement serverMovement = null;
//    private final EventManager serverEventManager;
//    private final Thread running_thread;

    public Game(int player_Count, ArrayList<Integer> player_ids) {
        this.Player_Count = player_Count;
        if (player_ids.size() != player_Count)
            throw new IllegalArgumentException("size of player_ids should equal to player_Count");
        this.player_ids = new ArrayList<>(player_ids);
        this.planePosition = new HashMap<>();
        initializePlanePosition();
        this.planeStacks = new ArrayList<>();
        this.dice_random = new SecureRandom();
        this.clients = new HashMap<>(4);
//        this.serverEventManager = new EventManager();
//        this.running_thread = new Thread(this::work);
    }

    private void initializePlanePosition() {
        for (int id : player_ids) {
            planePosition.put(id * 10 + 1, id * 100 + 99);
            planePosition.put(id * 10 + 2, id * 100 + 99);
            planePosition.put(id * 10 + 3, id * 100 + 99);
            planePosition.put(id * 10 + 4, id * 100 + 99);
        }
        System.out.println(planePosition);
    }

    public void announceStart() {
        synchronized (lock_obj) {
            var init_cbs = getChessboardStatus();
            clients.values().forEach(client -> client.UpdateClientChessBoard(init_cbs, ServerMovement.NoMovement));
            int start_player = player_ids.stream().min(Integer::compareTo).get();
            int dice_result = rolling_Dice(start_player);
            clients.get(start_player).Dice(DiceType.Fly, 2, dice_result);
            clients.values().stream()
                    .filter(c -> c.player_id != start_player)
                    .forEach(c -> c.ShowOtherDiceResult(start_player, DiceType.Fly, 2, dice_result));
        }
    }

    public boolean takeOff(int plane_id){
        synchronized (lock_obj){
            if(this.dice_player_id != plane_id / 10)
                return false;
            for (Map.Entry<Integer, Integer> entry : this.planePosition.entrySet()) {
                if(entry.getKey() / 10 == plane_id / 10)
                    if(entry.getValue() % 100 == 99){
                        // clear to take off
                        movePlane(plane_id, (plane_id / 10) * 100);
                        // TODO: Add ServerMovement
                        serverMovement = new ServerMovement(null);
                        updateClients();
                        this.dice_moved = true;
                        return true;
                    }
            }
            return false;
        }
    }

//    private void work() {
//        sleep(500);
//        // Test Ready
//        for (Client value : this.clients.values()) {
//            while (!value.isReady())
//                sleep(200);
//        }
//        while (true) {
//            var event = serverEventManager.get();
//            System.out.println("[Server] Received Event: " + event.toString());
//            switch (event.getType()) {
//                // TO DO: fill event filter
//            }
//        }
//    }
//
//    private void sleep(long ms){
//        try {
//            Thread.sleep(ms);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }

    /**
     * add Client, and automatically bind Game
     */
    public void addClient(Client client) {
        if (!player_ids.contains(client.player_id))
            return;
        this.clients.put(client.player_id, client);
        client.bindGame(this);
    }

    /**
     * Rolling Dice in Dice Type Fly, return an integer number in range [1,60]
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
            return true;
        }
    }

    /**
     * @return test passed
     */
    private boolean antiCheat(int plane_id, int step) {
        if (this.dice_player_id != plane_id / 10) // not your turn!!
            return false;
        if (this.dice_moved)     // you have moved!!
            return false;
        // Anti-Cheat done!

        var possibleMove = new ArrayList<Integer>();
        possibleMove.add(dice_first_result + dice_second_result);
        possibleMove.add(dice_first_result * dice_second_result);
        possibleMove.add(Math.abs(dice_first_result - dice_second_result));
        if (dice_first_result % dice_second_result == 0)
            possibleMove.add(dice_first_result / dice_second_result);
        if (dice_second_result % dice_first_result == 0)
            possibleMove.add(dice_second_result / dice_first_result);
        System.out.println("possible Movement:" + possibleMove);

        if (possibleMove.stream().noneMatch(mv -> mv == step))
            return false;

        // get current pos
        int current_pos = this.planePosition.get(plane_id) % 100;
        if (current_pos + step > 19)
            return false;
        // out of the plane
        return true;
    }


    /**
     * in baseMove, we don't consider there is any battle.
     * Guaranteed by battle judgement
     */
    private void baseMove(int plane_id, int step, boolean go_stack) {
        // check whether plane is in a stack
        var in_stacks = planeStacks.stream()
                .filter(stack -> stack.hasPlane(plane_id)).collect(Collectors.toList());
        PlaneStack stack = null;
        if (in_stacks.size() != 0)
            stack = in_stacks.get(0); // must have one;
        // stack == null -> not in any stack

        int start_pos = this.planePosition.get(plane_id);
        int start_index = PositionList.circleBoard.indexOf(start_pos);

        int end_index = (start_index + step) % 52;
        int end_pos = PositionList.all.get(PositionList.circleBoard.get(end_index)).ID;
        // get initial landing position

        int final_end_pos = end_pos;

        if (end_pos / 100 == plane_id / 10) {  // the same color -> ready to jump (first jump)
            // whether there are some planes in the first jump destination position
            tryBackPlanes(plane_id, end_pos, start_pos);

            // if the first jump is a fly, then it leads a double jump
            int middle_pos = getJumpDestination(end_pos);
            if (isFlyingPoint(end_pos)) {
                final_end_pos = getJumpDestination(middle_pos);
                tryBackPlanes(plane_id, middle_pos, end_pos);
                tryBackPlanes(plane_id, final_end_pos, middle_pos);
                movePlane(plane_id, final_end_pos);
                if (go_stack) {
                    tryStackPlanes(plane_id, middle_pos);
                    tryStackPlanes(plane_id, final_end_pos);
                }
                // TODO: Add ServerMovement to let client render more easlily
                return;
            } else { // the first jump is a simple jump
                if (isFlyingPoint(middle_pos)) { //the second is a flying jump
                    final_end_pos = getJumpDestination(middle_pos);
                    tryBackPlanes(plane_id, middle_pos, end_pos);
                    tryBackPlanes(plane_id, final_end_pos, middle_pos);
                    movePlane(plane_id, final_end_pos);
                    if (go_stack) {
                        tryStackPlanes(plane_id, middle_pos);
                        tryStackPlanes(plane_id, final_end_pos);
                    }
                } else { // the first jump is a simple jump, and no second jump
                    final_end_pos = middle_pos;
                    tryBackPlanes(plane_id, final_end_pos, end_pos);
                    movePlane(plane_id, final_end_pos);
                    if (go_stack) {
                        tryStackPlanes(plane_id, final_end_pos);
                    }
                }
            }
        } else { // not jump, just move (there won't be any battle needed, promised by antiCheat)
            movePlane(plane_id, final_end_pos);
        }
        // check win
        if (checkWin(plane_id / 10))
            declareWin(plane_id / 10);
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
            movePlane(diedPlane_id, (diedPlane_id / 100) * 100 + 99);
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
            if (planeStack.hasPlane(plane_id)) { // there is a stack in given position
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

    private void updateClients(){
        var cbs = getChessboardStatus();
        clients.values().forEach(c -> c.UpdateClientChessBoard(cbs,serverMovement));
    }

    /**
     * Only for debug
     */
    public void testMove(int plane_id, int step, boolean go_stack) {
        if (Main.DEBUG_MODE) {
            baseMove(plane_id, step, go_stack);
            updateClients();
            this.dice_moved = true;
        }
    }

    public void testModifyDiceResult(int dice_first_result, int dice_second_result){
        if(! Main.DEBUG_MODE)
            return;
        this.dice_first_result = dice_first_result;
        this.dice_second_result = dice_second_result;
    }

}
