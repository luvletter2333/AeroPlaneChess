package me.luvletter.planechess.client;

import me.luvletter.planechess.server.ChessBoardStatus;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Map;
import java.util.Timer;
import java.util.function.Function;

class Animation {
    private final Timer timer;
    private final ChessBoardStatus lastStatus;
    private final ChessBoardStatus endStatus;
    private final ArrayList<Movement> movements;
    private final Function<BufferedImage, Object> updateUI;

    public Animation(ChessBoardStatus lastStatus,
                     ChessBoardStatus endStatus,
                     Function<BufferedImage, Object> updateUI){
        this.lastStatus = lastStatus;
        this.endStatus = endStatus;
        this.timer = new Timer();
        this.movements = new ArrayList<>();
        this.updateUI = updateUI;
        //compare and give out which plane(s) need to be moved
        compareStatus();
        System.out.println(movements);
        //timer.
    }

    private void compareStatus(){
        var startposs = lastStatus.getPlanePosition();
        var endposs = endStatus.getPlanePosition();
        var iterator = startposs.entrySet().iterator();
        Map.Entry<Integer, Integer> entry;
        while (iterator.hasNext()){
            entry = iterator.next();
            // where the plane stop
            var end_pos = endStatus.getPlanePosition().get(entry.getKey());
            if(! end_pos.equals(entry.getValue())){
                // plane moved
                var move = new Movement(entry.getKey(), entry.getValue(), end_pos);
            }
        }
    }

    class Movement{
        private Integer plane_id;
        private Position start_position;
        private Position end_position;
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
