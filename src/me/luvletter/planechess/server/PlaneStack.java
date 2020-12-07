package me.luvletter.planechess.server;

import java.util.HashSet;

public class PlaneStack{
    private final HashSet<Integer> stacked_planes = new HashSet<>();

    public HashSet<Integer> getStacked_planes() {
        return stacked_planes;
    }

    public void addPlane(Integer plane_id){
        stacked_planes.add(plane_id);
    }
}
