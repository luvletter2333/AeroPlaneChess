package me.luvletter.planechess.server;

import java.util.ArrayList;
import java.util.HashSet;

public class PlaneStack {
    private final HashSet<Integer> stacked_planes = new HashSet<>();

    public HashSet<Integer> getStacked_planes() {
        return stacked_planes;
    }

    public PlaneStack addPlane(Integer plane_id) {
        stacked_planes.add(plane_id);
        return this;
    }

    public boolean hasPlane(int plane_id) {
        return stacked_planes.contains(plane_id);
    }

    public int planeCount() {
        return this.stacked_planes.size();
    }

    @Override
    public String toString() {
        return "PlaneStack{" +
                stacked_planes +
                '}';
    }
}
