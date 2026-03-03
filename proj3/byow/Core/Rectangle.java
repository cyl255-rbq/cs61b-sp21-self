package byow.Core;

import byow.Core.WorldGenerator.Direction;

import java.util.HashMap;
import java.util.Map;

public class Rectangle {
    private Positon p;
    private int width;
    private int height;
    private Map<Direction, Boolean> exits = new HashMap<>();

    Rectangle(Positon p, int width, int height) {
        this.p = p;
        this.width = width;
        this.height = height;
        exits.put(Direction.UP, false);
        exits.put(Direction.DOWN, false);
        exits.put(Direction.LEFT, false);
        exits.put(Direction.RIGHT, false);
    }

    public void setDirTrue(Direction p) {
        exits.put(p, true);
    }

    public Map<Direction, Boolean> exits() {
        return this.exits;
    }

    public Boolean isFull() {
        return exits.get(Direction.UP) && exits.get(Direction.DOWN)
                && exits.get(Direction.LEFT) && exits.get(Direction.RIGHT);

    }
}
