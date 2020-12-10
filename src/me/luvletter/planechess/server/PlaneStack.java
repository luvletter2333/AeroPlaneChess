package me.luvletter.planechess.server;

import java.util.ArrayList;

public class PlaneStack{
    private final ArrayList<Integer> stacked_planes = new ArrayList<>();

    public ArrayList<Integer> getStacked_planes() {
        return stacked_planes;
    }

    public void addPlane(Integer plane_id){
        stacked_planes.add(plane_id);
    }

    public boolean hasPlane(int plane_id){
        return stacked_planes.contains(plane_id);
    }
}
