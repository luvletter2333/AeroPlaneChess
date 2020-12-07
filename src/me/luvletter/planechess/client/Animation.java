package me.luvletter.planechess.client;

import jdk.jshell.spi.ExecutionControl;
import me.luvletter.planechess.server.ChessBoardStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

class Animation {
    private final Timer timer;
    private final ChessBoardStatus startStatus;
    private final ChessBoardStatus endStatus;
    private final Runnable updateUI;

    public Animation(ChessBoardStatus startStatus, ChessBoardStatus endStatus, Runnable updateUI){
        this.startStatus = startStatus;
        this.endStatus = endStatus;
        this.timer = new Timer();
        this.updateUI = updateUI;

        //compare and give out which plane(s) need to be moved
        compareStatus();
    }

    private void compareStatus(){
        var startpos = startStatus.getPosition();
        var endpos = endStatus.getPosition();
        var iterator = startpos.entrySet().iterator();
        Map.Entry<Integer, Integer> entry;
        while (iterator.hasNext()){
            entry = iterator.next();

        }
    }

}
