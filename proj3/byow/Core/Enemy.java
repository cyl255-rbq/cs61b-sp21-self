package byow.Core;

import java.util.List;

public class Enemy {
    private Position p;
    private List<Position> currentPath;

    public Enemy(Position p, List<Position> currentPath) {
        this.p = p;
        this.currentPath = currentPath;
    }

    public void updatePath(List<Position> newPath) {
        this.currentPath = newPath;
    }

    public void updatePosition(Position newPosition) {
        this.p = newPosition;
    }

    public Position position() {
        return this.p;
    }

    public List<Position> path() {
        return this.currentPath;
    }

}
