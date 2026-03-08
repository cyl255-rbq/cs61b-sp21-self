package byow.Core;

public class Position {
    int x;
    int y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int x() {
        return this.x;
    }

    public int y() {
        return this.y;
    }

    public Position shiftPosition(int x, int y) {
        return new Position(this.x + x, this.y + y);
    }

//public record Positon(int x, int y) {
//
//    public Positon shiftPosition(int x, int y) {
//        return new Positon(this.x + x, this.y + y);
//    }
//
//}
}

