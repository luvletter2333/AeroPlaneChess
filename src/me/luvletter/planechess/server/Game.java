package me.luvletter.planechess.server;

import me.luvletter.planechess.Main;
import me.luvletter.planechess.client.Position;
import me.luvletter.planechess.client.PositionList;

import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

public class Game {

    public final int Player_Count;
    private final ArrayList<Integer> player_ids;
    /**
     * Key -> plane ID
     * Value -> Position ID, such as 102 302
     * */
    private final HashMap<Integer, Integer> planePosition;
    private final ArrayList<PlaneStack> planeStacks;

    // Dice related:
    private int dice_player_id;
    private int dice_first_result;
    private int dice_second_result;
    private boolean dice_moved = false;
    private Random dice_random;

    private boolean has_won = false;
    private int win_player_id = 0;

    private final Object lock_obj = new Object();


    public Game(int player_Count, ArrayList<Integer> player_ids) {
        this.Player_Count = player_Count;
        if (player_ids.size() != player_Count)
            throw new IllegalArgumentException("size of player_ids should equal to player_Count");
        this.player_ids = new ArrayList<>(player_ids);
        this.planePosition = new HashMap<>();
        initializePlanePosition();
        this.planeStacks = new ArrayList<>();
        this.dice_random = new SecureRandom();
    }

    private void initializePlanePosition() {
        for (int id : player_ids) {
            planePosition.put(id * 10 + 1, id * 100);
            planePosition.put(id * 10 + 2, id * 100);
            planePosition.put(id * 10 + 3, id * 100);
            planePosition.put(id * 10 + 4, id * 100);
        }
        System.out.println(planePosition);
    }

    /**
     * Rolling Dice, return an integer number in range [1,60]
     *
     * @return result / 10 -> first. result % 10 -> second
     */
    public int rolling_Dice(int player_id) {
        synchronized (lock_obj) {
            if (!this.dice_moved)
                return 0; // not time for you to dice!
            this.dice_player_id = player_id;
            this.dice_moved = false;
            this.dice_first_result = this.dice_random.nextInt(6) + 1;
            this.dice_second_result = this.dice_random.nextInt(6) + 1;
            return this.dice_first_result * 10 + this.dice_second_result;
        }
    }

    /**
     * Move a plane with given id `step` steps
     * Please check whether data are valid in server side
     *
     * @return false -> no cheat! or battle needed, true -> accepted
     *
     */
    public boolean move(int plane_id, int step, boolean go_stack) {
        synchronized (lock_obj) {
            if(!antiCheat(plane_id, step))
                return false;   // anti-cheat test not passed
            // validate step done!

            int start_pos = this.planePosition.get(plane_id);
            int start_index = PositionList.circleBoard.indexOf(start_pos);

            int end_index = ( start_index + step ) % 52;
            int end_pos = PositionList.all.get(PositionList.circleBoard.get(end_index)).ID;

            // judge whether there will be a battle
            // the end position has planes with **different** color
            // in jump mode, planes are sent to their home directly, no battle needed
            if (this.planePosition.containsValue(end_pos)
                    && (end_pos / 100) != (plane_id / 10))
                return false;

            baseMove(plane_id, step, go_stack);
            this.dice_moved = true;
            return true;
        }
    }

    /**
     * @return test passed
     * */
    private boolean antiCheat(int plane_id, int step){
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
        if(current_pos + step > 19)
            return false;
        // out of the plane
        return true;
    }

    
    private void baseMove(int plane_id, int step, boolean go_stack){
        // TODO: go_stack
        // check whether plane is in a stack
        var in_stacks = planeStacks.stream()
                .filter(stack -> stack.hasPlane(plane_id)).collect(Collectors.toList());
        PlaneStack stack = null;
        if (in_stacks.size() != 0)
            stack = in_stacks.get(0); // must have one;
        // stack == null -> not in any stack

        int start_pos = this.planePosition.get(plane_id);
        int start_index = PositionList.circleBoard.indexOf(start_pos);

        int end_index = ( start_index + step ) % 52;
        int end_pos = PositionList.all.get(PositionList.circleBoard.get(end_index)).ID;
        // get initial landing position

        int final_end_pos = end_pos;

        if (end_pos / 100 == plane_id / 10){  // the same color -> ready to jump (first jump)
            // whether there are some planes in the first jump destination position
            tryBackPlanes(plane_id, end_pos, start_pos);

            // if the first jump is a fly, then it leads a double jump
            int middle_pos = getJumpDestination(end_pos);
            if (isFlyingPoint(end_pos)){
                final_end_pos = getJumpDestination(middle_pos);
                tryBackPlanes(plane_id, middle_pos, end_pos);
                tryBackPlanes(plane_id, final_end_pos, middle_pos);
                movePlane(plane_id, final_end_pos);
                // TODO: Add ServerMovement to let client render
                return;
            }
            else{ // the first jump is a simple jump
                if(isFlyingPoint(middle_pos)){ //the second is a flying jump
                    final_end_pos = getJumpDestination(middle_pos);
                    tryBackPlanes(plane_id, middle_pos, end_pos);
                    tryBackPlanes(plane_id, final_end_pos, middle_pos);
                    movePlane(plane_id, final_end_pos);
                }
                else{ // the first jump is a simple jump, and no second jump
                    final_end_pos = middle_pos;
                    tryBackPlanes(plane_id, final_end_pos, end_pos);
                    movePlane(plane_id, final_end_pos);
                }
            }
        }
        else{ // not jump, just move (there won't be any battle needed, promised by antiCheat)
            movePlane(plane_id, final_end_pos);
        }
    }

    /**
     * check whether given point is the Flying Point
     * */
    private boolean isFlyingPoint(int start_pos){
        return start_pos % 10 == 5;
        // Flying Points:
        // 105, 205, 305, 405
    }

    /**
     * get the Jump Destination Position's ID
     * Please make sure there is a jump
     * */
    private int getJumpDestination(int start_pos){
        if(isFlyingPoint(start_pos)){
            return start_pos + 3;
        }
        // not flying point
        return start_pos + 1;
    }

    private void movePlane(int plane_id, int destPos){
        if(this.planeStacks.stream().noneMatch(planeStack -> planeStack.hasPlane(plane_id))) {
            // the plane isn't in any stack, just update position_id
            this.planePosition.put(plane_id, destPos);
        }
        else{
            for (PlaneStack planeStack : this.planeStacks) {
                if(planeStack.hasPlane(plane_id)) {
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
     * */
    private void tryBackPlanes(int source_plane_id, int dest_pos, int source_pos){
        var diedPlanes = new ArrayList<Integer>(); // plane's ID which is going to die
        this.planePosition.forEach((pid, ppos) -> {
            if(ppos.equals(dest_pos) && pid / 10 != source_plane_id / 10) // not in the same color
                diedPlanes.add(pid);
        });

        // flying across the final approach line
        if(source_pos!=0 && isFlyingPoint(source_pos)){
            switch (source_plane_id / 10){
                case 1: tryBackPlanes(source_plane_id, 316, 0);
                case 2: tryBackPlanes(source_plane_id, 216, 0);
                case 3: tryBackPlanes(source_plane_id, 116, 0);
                case 4: tryBackPlanes(source_plane_id, 416,0);
            }
        }
        for (Integer diedPlane_id : diedPlanes) {
            movePlane(diedPlane_id, (diedPlane_id / 100) * 100 + 99);
        }
        // un-stack
        if(diedPlanes.size() > 0) {
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
        if(Main.DEBUG_MODE)
            System.out.println("Removed Planes:" + diedPlanes.toString());
    }

    /**
     * check whether `player_id` has won this game
     * */
    private boolean checkWin(int player_id){
        // TODO: finish checkWin method
        this.has_won = true;
        this.win_player_id = player_id;
        return false;
    }

    /**
     * Request From Client
     */
    public ChessBoardStatus getChessboardStatus(){
        synchronized (lock_obj){
            return new ChessBoardStatus(this.Player_Count, this.planePosition, this.planeStacks);
        }
    }

}
