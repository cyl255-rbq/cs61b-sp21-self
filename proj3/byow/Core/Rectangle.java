package byow.Core;

public class Rectangle {
    private Position p;
    private int width;
    private int height;

    Rectangle(Position p, int width, int height) {
        this.p = p;
        this.width = width;
        this.height = height;
    }

    public int width() {
        return this.width;
    }

    public int height() {
        return this.height;
    }

    public Position positon() {
        return this.p;
    }
}
