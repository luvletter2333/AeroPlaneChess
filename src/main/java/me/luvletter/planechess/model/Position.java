package me.luvletter.planechess.model;

// Block on chessboard
public final class Position {
    public final int ID;
    public final PlayerColor Color;
    public final Point Point;

    public Position(int ID, int X, int Y){
        this.ID = ID;
        this.Color = getColor();
        this.Point = new Point(X, Y);
    }
    private PlayerColor getColor(){
        return switch (ID / 100){
            case 1 -> PlayerColor.Red;
            case 2 -> PlayerColor.Yellow;
            case 3 -> PlayerColor.Blue;
            case 4 -> PlayerColor.Green;
            default -> null;
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return ID == position.ID;
    }

    @Override
    public String toString() {
        return "Position{" +
                "ID=" + ID +
                ", Color=" + Color +
                '}';
    }
}
