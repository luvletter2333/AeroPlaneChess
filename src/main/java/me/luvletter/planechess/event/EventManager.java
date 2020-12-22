package me.luvletter.planechess.event;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class EventManager {
    private BlockingQueue<Event> blockingQueue;

    public EventManager() {
        this.blockingQueue = new LinkedBlockingQueue<>(25);
    }

    public void push(Event e) {
        try {
            blockingQueue.put(e);
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        }
    }

    public Event get() {
        try {
            return blockingQueue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void clearEvents() {
        this.blockingQueue.clear();
    }

    public int size() {
        return this.blockingQueue.size();
    }
}
