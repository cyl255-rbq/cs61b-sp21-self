package byow.Core;

public class Rectangle {
    private Positon p;
    private int width;
    private int height;

    Rectangle(Positon p, int width, int height) {
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

    public Positon positon() {
        return this.p;
    }
}
