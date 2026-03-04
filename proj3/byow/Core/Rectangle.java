package byow.Core;

import byow.Core.WorldGenerator.Direction;

import java.util.HashMap;
import java.util.Map;

public class Rectangle {
    private Positon p;
    private int width;
    private int height;
    private boolean isFull;
    private Map<Direction, Boolean> edges = new HashMap<>();

    Rectangle(Positon p, int width, int height) {
        this.p = p;
        this.width = width;
        this.height = height;
        this.isFull = false;
        edges.put(Direction.UP, false);
        edges.put(Direction.DOWN, false);
        edges.put(Direction.LEFT, false);
        edges.put(Direction.RIGHT, false);
    }

    public void setDirTrue(Direction p) {
        edges.put(p, true);
    }

    public Map<Direction, Boolean> edges() {
        return this.edges;
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

    public Boolean isFull() {
        return edges.get(Direction.UP) && edges.get(Direction.DOWN)
                && edges.get(Direction.LEFT) && edges.get(Direction.RIGHT);

    }
}
