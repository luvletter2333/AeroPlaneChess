package me.luvletter.planechess.server;

import me.luvletter.planechess.client.Position;
import me.luvletter.planechess.client.PositionList;

import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

public class Game {

    public final int Player_Count;
    protected final ArrayList<Integer> player_ids;
    /**
     * Key -> plane ID
     * Value -> Position ID, such as 102 302
     * */
    protected final HashMap<Integer, Integer> planePosition;
    protected final ArrayList<PlaneStack> planeStacks;

    // Dice related:
    protected int dice_player_id;
    protected int dice_first_result;
    protected int dice_second_result;
    protected boolean dice_moved = false;
    protected Random dice_random;

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
     * @return whether this move try is accepted
     */
    public boolean move(int plane_id, int step, boolean go_stack) {
        synchronized (lock_obj) {
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
            // return false if not match all possible movement
            // validate step done!

            baseMove(plane_id, step, go_stack);

            this.dice_moved = true;
            return true;
        }
    }

    protected void baseMove(int plane_id, int step, boolean go_stack){
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
        Position end_pos = PositionList.all.get(PositionList.circleBoard.get(end_index));
        // get landing position

        if (end_pos.Color.getIntValue() == plane_id / 10){
            // the same color
        }

        // TODO: finish stack judgement and move

        //planePosition.remove(plane_id);
       // planePosition.put(plane_id, destPos);
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
