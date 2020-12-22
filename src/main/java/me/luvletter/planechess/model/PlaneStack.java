package me.luvletter.planechess.model;

import java.util.HashSet;
import java.util.Set;

public class PlaneStack {
    private final HashSet<Integer> stacked_planes;

    public PlaneStack(int plane_id) {
        this.stacked_planes = new HashSet<>(4);
        this.stacked_planes.add(plane_id);
    }

    public PlaneStack(Set<Integer> planes){
        this.stacked_planes = new HashSet<>(planes);
    }

    private PlaneStack(HashSet<Integer> planes){
        this.stacked_planes = new HashSet<>(planes);
    }

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

    public PlaneStack deepCopy(){
        return new PlaneStack(this.stacked_planes);
    }
}
