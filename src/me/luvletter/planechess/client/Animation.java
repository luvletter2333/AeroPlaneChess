package me.luvletter.planechess.client;

import me.luvletter.planechess.server.ChessBoardStatus;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Map;
import java.util.function.Function;

class Animation {
    private final ChessBoardStatus startStatus;
    private final ChessBoardStatus endStatus;
    private final ArrayList<Movement> movements;
    private final ArrayList<Movement> eat_movements;
    private final DrawHelper drawHelper;
    private BufferedImage base_image;

    public Animation(ChessBoardStatus lastStatus,
                     ChessBoardStatus endStatus){
        this.startStatus = lastStatus;
        this.endStatus = endStatus;
        this.movements = new ArrayList<>();
        this.eat_movements = new ArrayList<>();
        this.drawHelper = new DrawHelper();
        //compare and give out which plane(s) need to be moved
        var list =  compareStatus();
        System.out.println(movements);
        generateBaseImage(list);
    }

    /**
     * Generate Base Image to Reduce drawing time;
     * */
    private void generateBaseImage(ArrayList<Integer> not_move_list) {
        not_move_list.forEach(plane_id -> {
            drawHelper.Draw(plane_id, endStatus.getPlanePosition().get(plane_id));
        });
        this.base_image = drawHelper.getResultImage();
    }

    public BufferedImage getBase_image() {
        return base_image;
    }

    /**
     * block thread and draw
     * */
    public void Animate(Drawable_JPanel dpanel){
        //TODO: fill Animate method
    }

    /**
     * Compare start_Plane_Positions with end_Plane_Positions, add into movements
     * Calculate Eat_Movements
     * @return list containing all planes which are not going to move.
     * */
    private ArrayList<Integer> compareStatus(){
        var startposs = startStatus.getPlanePosition();
        var iterator = startposs.entrySet().iterator();
        var list = new ArrayList<Integer>(); // list containing all planes which are not going to move.
        Map.Entry<Integer, Integer> entry;
        while (iterator.hasNext()){
            entry = iterator.next();
            // where the plane stop
            var end_pos = endStatus.getPlanePosition().get(entry.getKey());
            if(! end_pos.equals(entry.getValue())){
                // plane moved
                var move = new Movement(entry.getKey(), entry.getValue(), end_pos);
                movements.add(move);
                System.out.println(move);
            }
            else{ //not moved, add to list
                list.add(entry.getKey());
            }
        }
        return list;
    }

    class Movement{
        private final Integer plane_id;
        private final Position start_position;
        private final Position end_position;
        public int step;
        public final static int MAX_STEP = 5;
        public Movement(Integer plane_id, Integer start_pos, Integer end_pos){
            this.plane_id = plane_id;
            this.start_position = PositionList.all.get(start_pos);
            this.end_position = PositionList.all.get(end_pos);
            this.step = 0;
        }

        @Override
        public String toString() {
            return "Movement{" +
                    "plane_id=" + plane_id +
                    ", start_position=" + start_position +
                    ", end_position=" + end_position +
                    ", step=" + step +
                    '}';
        }
    }

}
