package byow.Core;

public class PathNode implements Comparable<PathNode>{
    private int manhattan;
    private int distTo;
    private Position p;


    public PathNode(Position p, int distTo, int manhattan) {
        this.distTo = distTo;
        this.manhattan = manhattan;
        this.p = p;
    }

    public static int manhattan(Position start, Position target) {
        return Math.abs(start.x() - target.x()) + Math.abs(start.y() - target.y());
    }

    public Position position() {
        return this.p;
    }

    public int distTo() {
        return this.distTo;
    }

    @Override
    public int compareTo(PathNode o) {
        return this.distTo + this.manhattan - o.distTo - o.manhattan;
    }
}
