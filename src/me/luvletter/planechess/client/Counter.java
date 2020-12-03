package me.luvletter.planechess.client;

class Counter {
    private int value = 0;

    /**
     * from 1
     * */
    public int increase(){
        value++;
        return value;
    }

    public int getValue() {
        return value;
    }

    public void clear(){
        value = 0;
    }
}
