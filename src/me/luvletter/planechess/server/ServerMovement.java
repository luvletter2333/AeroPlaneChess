package me.luvletter.planechess.server;

import java.util.ArrayList;

public class ServerMovement {
    // TODO: create Server Movement

    public final boolean hasMovement;
    public ServerMovement(ArrayList<Object> todo){
        this.hasMovement = true;
    }

    private ServerMovement(boolean hasMovement){
        this.hasMovement = hasMovement;
    }
    public static final ServerMovement NoMovement = new ServerMovement(false);


}
