package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.*;

import static byow.Core.Engine.HEIGHT;
import static byow.Core.Engine.WIDTH;

public class WorldGenerator {
    private int width;
    private int height;
    private long seed;
    private Random random;
    private TETile[][] world;
    private ArrayList<Rectangle> alternative = new ArrayList<>();
    private int roomNum;

    WorldGenerator(int width, int height, int seed) {
        this.width = width;
        this.height = height;
        this.seed = seed;
        this.random = new Random(seed);
        this.world = new TETile[width][height];
    }

    public void fillWithNothing(TETile[][] tiles) {
        int height = tiles[0].length;
        int width = tiles.length;
        for (int x = 0; x < width; x += 1) {
            for (int y = 0; y < height; y += 1) {
                tiles[x][y] = Tileset.NOTHING;
            }
        }
    }

    private void drawRow(TETile[][] tiles, Positon p, TETile tile, int length) {
        int signum = Integer.signum(length);
        for (int i = 0; i < Math.abs(length); i += 1) {
            tiles[p.x() + signum * i][p.y()] = tile;
        }
    }

    private void drawColumn(TETile[][] tiles, Positon p, TETile tile, int length) {
        int signum = Integer.signum(length);
        for (int i = 0; i < Math.abs(length); i += 1) {
            tiles[p.x()][p.y() + signum * i] = tile;
        }
    }

    private void fillWithFloor(TETile[][] tiles, Positon p, int width, int height) {
        //int widthSign = Integer.signum(width);
        int heightSign = Integer.signum(height);
        //p = p.shiftPosition(widthSign, heightSign);
        for (int i = 0; i < Math.abs(height); i += 1) {
            drawRow(tiles, p, Tileset.FLOOR, width);
            p = p.shiftPosition(0, heightSign);
        }
    }

    public boolean outEdges(Positon p, int width, int height) {
        return ((p.x() + width > WIDTH - 1) || (p.x() + width < 0)) ||
                ((p.y() + height > HEIGHT - 1) || (p.y() + height < 0)) ||
                (p.x() == 0 || p.y() == 0);
    }

    public boolean overlay(TETile[][] tiles, Positon p, int width, int height) {
        int x = p.x();
        int y = p.y();
        int widthSign = Integer.signum(width);
        int heightSign = Integer.signum(height);
        for (int i = 0; i < Math.abs(width); i += 1) {
            for (int j = 0; j < Math.abs(height); j += 1) {
                if (tiles[x + i * widthSign][y + j * heightSign] != Tileset.NOTHING) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean collision(TETile[][] tiles, Positon p, int width, int height) {
        return outEdges(p, width, height) || overlay(tiles, p, width, height);
    }

    public void addRoomHelper(TETile[][] tiles, Positon p, int width, int height) {
        if (Math.abs(width) < 1 || Math.abs(height) < 1) {
            return;
        }

//        drawRow(tiles, p, Tileset.WALL, width);
//        drawColumn(tiles, p, Tileset.WALL, height);
//        drawRow(tiles, p.shiftPosition(0, height - Integer.signum(height)), Tileset.WALL, width);
//        drawColumn(tiles, p.shiftPosition(width - Integer.signum(width), 0), Tileset.WALL, height);
        fillWithFloor(tiles, p, width, height);
    }

    public Rectangle addRoomAssist(TETile[][] tiles, Positon p, int widthSign, int heightSign) {
        int width = RandomUtils.uniform(random, 1, 7) * widthSign;
        int height = RandomUtils.uniform(random, 1, 7) * heightSign;
//        width =  3;
//        height = 3;
        if (collision(tiles, p, width, height)) {
            return null;
        }
        addRoomHelper(tiles, p, width, height);
        Rectangle neighbour = new Rectangle(p, width, height);
        alternative.add(neighbour);
        return neighbour;
    }

//    public void addRoom(TETile[][] tiles, Positon p) {
//        int widthSign = RandomUtils.bernoulli(random) ? 1 : -1;
//        int heightSign = RandomUtils.bernoulli(random) ? 1 : -1;
//        addRoomAssist(tiles, p, widthSign, heightSign);
//    }

    public enum Shape {
        HORIZONTAL, VERTICAL
    }

    private Shape randomDir() {
        return RandomUtils.bernoulli(random) ? Shape.HORIZONTAL : Shape.VERTICAL;
    }

    private void addHallwaysHelper(TETile[][] tiles, Positon p, int length, int anotherLen, Shape dir) {
        if (Math.abs(length) < 1 || Math.abs(anotherLen) < 1) {
            return;
        }
        if (dir == Shape.HORIZONTAL) {
//            drawRow(tiles, p, Tileset.WALL, length);
//            drawColumn(tiles, p, Tileset.WALL, anotherLen);
//            drawRow(tiles, p.shiftPosition(0, anotherLen - anotherSign), Tileset.WALL, length);
//            drawColumn(tiles, p.shiftPosition(length - lengthSign, 0), Tileset.WALL, anotherLen);
            fillWithFloor(tiles, p, length, anotherLen);
        }
        else {
//            drawRow(tiles, p, Tileset.WALL, anotherLen);
//            drawColumn(tiles, p, Tileset.WALL, length);
//            drawRow(tiles, p.shiftPosition(0, length - lengthSign), Tileset.WALL, anotherLen);
//            drawColumn(tiles, p.shiftPosition(anotherLen - anotherSign, 0), Tileset.WALL, length);
            fillWithFloor(tiles, p, anotherLen, length);
        }
    }

    private boolean hallwaysCollision(TETile[][] tiles, Positon p, int length, int anotherLen, Shape dir) {
        if (dir == Shape.HORIZONTAL) {
            return outEdges(p, length, anotherLen) || overlay(tiles, p, length, anotherLen);
        } else if (dir == Shape.VERTICAL) {
            return outEdges(p, anotherLen, length) || overlay(tiles, p, anotherLen, length);
        }
        return false;
    }

    public Rectangle addHallwaysAssist(TETile[][] tiles, Positon p, int lengthSign, int anotherSign) {
        int anotherLen = RandomUtils.uniform(random, 1, 3) * anotherSign;
        int length = RandomUtils.uniform(random, 1, 13) * lengthSign;
        Shape dir = randomDir();
//        anotherLen =  3;
//        length =  6;
//        dir = Shape.HORIZONTAL;
        if (hallwaysCollision(tiles, p, length, anotherLen, dir)) {
            return null;
        }
        addHallwaysHelper(tiles, p, length, anotherLen, dir);
        Rectangle neighbour = new Rectangle(p, width, height);
        alternative.add(neighbour);
        return neighbour;
    }

    public Rectangle addRectangle(TETile[][] tiles, Positon p, int widthSign, int heightSign) {
        if (RandomUtils.bernoulli(random)) {
            return addRoomAssist(tiles, p, widthSign, heightSign);
        } else {
            return addHallwaysAssist(tiles, p, widthSign, heightSign);
        }
    }

    public enum Direction {
        LEFT, RIGHT, UP, DOWN
    }

    private int randomNeighbourX(Rectangle room) {
        int xRange = room.width();
        if (xRange > 0) {
            return RandomUtils.uniform(random, 0, xRange);
        } else {
            return RandomUtils.uniform(random, xRange + 1, 1);
        }
    }

    private int randomNeighbourY(Rectangle room) {
        int yRange = room.height();
        if (yRange > 0) {
            return RandomUtils.uniform(random, 0, yRange);
        } else {
            return RandomUtils.uniform(random, yRange + 1, 1);
        }
    }

    private void bothTrue(Rectangle room, Rectangle neighbour, Direction dir) {
        if (neighbour == null) {
            return;
        }
        Direction neighbourdir = switch (dir) {
            case UP -> Direction.DOWN;
            case DOWN -> Direction.UP;
            case LEFT -> Direction.RIGHT;
            case RIGHT -> Direction.LEFT;
        };
        room.setDirTrue(dir);
        neighbour.setDirTrue(neighbourdir);
        if (room.isFull()) {
            alternative.remove(room);
        }
    }
    
    private void addNeighbour(TETile[][] tiles, Rectangle room, Direction dir) {
        Positon p = room.positon();
        int width = room.width();
        int height = room.height();
        int widthSign = Integer.signum(width);
        int heightSign = Integer.signum(height);
        int randomSign = RandomUtils.bernoulli(random) ? 1 : -1;
        int xRange = randomNeighbourX(room);
        int yRange = randomNeighbourY(room);
        Rectangle neighbour = null;
        switch (dir) {
            case UP -> {
                if (heightSign > 0) {
                    p = p.shiftPosition(xRange, height);
                } else {
                    p = p.shiftPosition(xRange, 1);
                }
                //tiles[p.x()][p.y()] = Tileset.SAND;
                //todo 两个房间添加关系，上面是下面的上，下面是上面的下，应该需要传递Rectangele room 和 Direction dir
                neighbour = addRectangle(tiles, p, randomSign, 1);
            }
            case DOWN -> {
                if (heightSign < 0) {
                    p = p.shiftPosition(xRange, height);
                } else {
                    p = p.shiftPosition(xRange, -1);
                }
                neighbour = addRectangle(tiles, p, randomSign, -1);
            }
            case LEFT -> {
                if (widthSign < 0) {
                    p = p.shiftPosition(width, yRange);
                } else {
                    p = p.shiftPosition(-1, yRange);
                }
                neighbour = addRectangle(tiles, p, -1, randomSign);
            }
            case RIGHT -> {
                if (widthSign > 0) {
                    p = p.shiftPosition(width, yRange);
                } else {
                    p = p.shiftPosition(1, yRange);
                }
                neighbour = addRectangle(tiles, p, 1, randomSign);
            }
        }
        bothTrue(room, neighbour, dir);
        if (room.isFull()) {
            alternative.remove(room);
        }
    }

    private boolean inRange(int width, int height) {
        return width >= 0 && width < WIDTH
                && height >=0 && height < HEIGHT;
    }

    private boolean aroundFloor(TETile[][] tiles, int width, int height) {
        for (int h = -1; h < 2; h += 1) {
            for (int w = -1; w < 2; w += 1) {
                if (inRange(width + w, height + h) &&
                        tiles[width + w][height + h] == Tileset.FLOOR) {
                    return true;
                }
            }
        }
        return false;
    }

    private void fillWall(TETile[][] tiles) {
        for (int h = 0; h < HEIGHT; h += 1) {
            for (int w = 0; w < WIDTH; w += 1) {
                if (tiles[w][h] == Tileset.NOTHING && aroundFloor(tiles, w, h)) {
                    tiles[w][h] = Tileset.WALL;
                }
            }
        }
    }

    public void extendRectangle(TETile[][] tiles, Rectangle room) {
        Map<Direction, Boolean> edges = room.edges();
        for (Direction d : edges.keySet()) {
            if (!edges.get(d) && RandomUtils.bernoulli(random)) {
                addNeighbour(tiles, room, d);
            }
        }
//        if (room.checkFull()) {
//
//        }
    }

    public TETile[][] generate() {

        //todo
        return world;
    }


    public static void main(String[] args) {
        TERenderer ter = new TERenderer();
        ter.initialize(80, 30);
        TETile[][] world = new TETile[80][30];
        WorldGenerator a = new WorldGenerator(80,30,16344);
        a.fillWithNothing(world);
        //Positon p = new Positon(80, 30);
        Positon p0 =new Positon(0,0);
        Positon p1 = new Positon(1,1);
        Positon p2 = new Positon(40, 15);
        Positon p3 = new Positon(46,21);
        //范围是1-78
        Positon p4 = new Positon(20,7);
        Positon p5 = new Positon(60,20);
        Positon pEdge = new Positon(76,26);
        Positon pEdge2 = new Positon(74,28);
        Positon pEdge3 = new Positon(5,2);
        Positon pEdge5= new Positon(3,1);   //a.addRoom(world,pEdge);
        Positon pHall = new Positon(6,3);
        Positon pHall2 = new Positon(73,26);

        a.addRoomAssist(world,p2, -1, -1);
//        a.addRoomAssist(world,p4, -1, -1);
        a.addNeighbour(world, a.alternative.get(0),Direction.LEFT);
         a.addNeighbour(world, a.alternative.get(0),Direction.RIGHT);
         a.addNeighbour(world, a.alternative.get(0),Direction.UP);
         a.addNeighbour(world, a.alternative.get(0),Direction.DOWN);
//        a.addNeighbour(world, a.alternative.get(1),Direction.LEFT);
//        a.addNeighbour(world, a.alternative.get(1),Direction.RIGHT);
//        a.addNeighbour(world, a.alternative.get(1),Direction.UP);
//        a.addNeighbour(world, a.alternative.get(1),Direction.DOWN);
        //a.fillWall(world);
        Map<Direction, Boolean> edge = a.alternative.get(0).edges();
        for (Direction c : edge.keySet()) {
            System.out.println(c);
            System.out.println(edge.get(c));
        }

        //System.out.println(a.alternative.get(1).isFull());
        //a.addRoomHelper(world,p2,4,4);
//        a.addHallwaysHelper(world,p2,6,3,Shape.HORIZONTAL);
//        a.addHallwaysHelper(world,p2,-6,-3,Shape.HORIZONTAL);
//        a.addHallways(world,pHall2);
//        a.addHallways(world,p1);
//        a.addHallways(world,p2);
//        a.addHallways(world,p3);
//        a.addHallways(world,p4);
//        a.addHallways(world,p5);

//        a.addRoom(world,p2);
//        a.addRoom(world,p3);
//        a.addRoom(world,p4);
//        a.addRoom(world,p1);


        ter.renderFrame(world);
    }
}
