package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.ArrayList;
import java.util.Random;

import static byow.Core.Engine.HEIGHT;
import static byow.Core.Engine.WIDTH;

public class WorldGenerator {
    private int width;
    private int height;
    private long seed;
    private Random random;
    private TETile[][] world;
    private ArrayList<Rectangle> alternative = new ArrayList<>();
    private int size;

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
        int widthSign = Integer.signum(width);
        int heightSign = Integer.signum(height);
        p = p.shiftPosition(widthSign, heightSign);
        for (int i = 0; i < Math.abs(height) - 2; i += 1) {
            drawRow(tiles, p, Tileset.FLOOR, width - 2 * widthSign);
            p = p.shiftPosition(0, heightSign);
        }
    }

    public boolean outEdges(Positon p, int width, int height) {
        return ((p.x() + width > WIDTH) || (p.x() + width < -1)) ||
                ((p.y() + height > HEIGHT) || (p.y() + height < -1));
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
        if (Math.abs(width) <= 2 || Math.abs(height) <=2) {
            return;
        }
        drawRow(tiles, p, Tileset.WALL, width);
        drawColumn(tiles, p, Tileset.WALL, height);
        drawRow(tiles, p.shiftPosition(0, height - Integer.signum(height)), Tileset.WALL, width);
        drawColumn(tiles, p.shiftPosition(width - Integer.signum(width), 0), Tileset.WALL, height);
        fillWithFloor(tiles, p, width, height);
    }

    public void addRoom(TETile[][] tiles, Positon p) {
        int sign = RandomUtils.bernoulli(random) ? 1 : -1;
        int width = RandomUtils.uniform(random, 3, 9) * sign;
        sign = RandomUtils.bernoulli(random) ? 1 : -1;
        int height = RandomUtils.uniform(random, 3, 9) * sign;
        //width =  3;
        //height =  3;
        if (collision(tiles, p, width, height)) {
            return;
        }
        addRoomHelper(tiles, p, width, height);
        alternative.add(new Rectangle(p, width, height));
    }

    public enum Shape {
        HORIZONTAL, VERTICAL
    }

    private Shape randomDir() {
        return RandomUtils.bernoulli(random) ? Shape.HORIZONTAL : Shape.VERTICAL;
    }

    private void addHallwaysHelper(TETile[][] tiles, Positon p, int length, int anotherLen, Shape dir) {
        if (Math.abs(length) <=2 || Math.abs(anotherLen) <=2) {
            return;
        }
        int lengthSign = Integer.signum(length);
        int anotherSign = Integer.signum(anotherLen);
        if (dir == Shape.HORIZONTAL) {
            drawRow(tiles, p, Tileset.WALL, length);
            drawColumn(tiles, p, Tileset.WALL, anotherLen);
            drawRow(tiles, p.shiftPosition(0, anotherLen - anotherSign), Tileset.WALL, length);
            drawColumn(tiles, p.shiftPosition(length - lengthSign, 0), Tileset.WALL, anotherLen);
            fillWithFloor(tiles, p, length, anotherLen);
        }
        else {
            drawRow(tiles, p, Tileset.WALL, anotherLen);
            drawColumn(tiles, p, Tileset.WALL, length);
            drawRow(tiles, p.shiftPosition(0, length - lengthSign), Tileset.WALL, anotherLen);
            drawColumn(tiles, p.shiftPosition(anotherLen - anotherSign, 0), Tileset.WALL, length);
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

    public void addHallways(TETile[][] tiles, Positon p) {
        int sign = RandomUtils.bernoulli(random) ? 1 : -1;
        int anotherLen = RandomUtils.uniform(random, 3, 5) * sign;
        sign = RandomUtils.bernoulli(random) ? 1 : -1;
        int length = RandomUtils.uniform(random, 3, 15) * sign;
        Shape dir = randomDir();
        //anotherLen = -3;
        //length = -6;
        dir = Shape.HORIZONTAL;
        if (hallwaysCollision(tiles, p, length, anotherLen, dir)) {
            return;
        }
        addHallwaysHelper(tiles, p, length, anotherLen, dir);
    }

    public enum Direction {
        LEFT, RIGHT, UP, DOWN
    }

    public void extendRectangle(Rectangle room) {
        
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
        Positon p2 = new Positon(40, 15);
        Positon p3 = new Positon(46,21);
        //范围是0-79
        Positon p4 = new Positon(20,7);
        Positon pEdge = new Positon(77,27);
        Positon pEdge2 = new Positon(74,28);
        Positon pEdge3 = new Positon(5,2);
        //a.addRoom(world,pEdge);
        //a.addRoom(world,p2);
        //a.drawColumn(world,p2,Tileset.UNLOCKED_DOOR,-1);
        //a.addRoomHelper(world, p2, 5, 5);
        a.addHallwaysHelper(world,pEdge3,-3,-3,Shape.HORIZONTAL);
        a.addHallwaysHelper(world,p2,3,3,Shape.HORIZONTAL);
        a.addHallways(world,pEdge3);
        a.addHallways(world,p4);
//        a.addRoomHelper(world, p3 ,-3,-3);
//        Positon p4 = new Positon(1,1);
//        a.addRoomHelper(world, p4 ,-3,-3);
//        Random random = new Random(12345); // 固定种子便于观察
//        System.out.println(RandomUtils.bernoulli(random)); // 第一次：可能 true
//        System.out.println(RandomUtils.bernoulli(random)); // 第二次：可能 false
//        System.out.println(RandomUtils.bernoulli(random)); // 第三次：可能 true
//        System.out.println(RandomUtils.bernoulli(random)); // 第三次：可能 true
//        System.out.println(RandomUtils.bernoulli(random)); // 第三次：可能 true
//        System.out.println(RandomUtils.bernoulli(random)); // 第三次：可能 true
//        System.out.println(RandomUtils.bernoulli(random)); // 第三次：可能 true
//        System.out.println(RandomUtils.bernoulli(random)); // 第三次：可能 true
        //a.addRoomHelper(world, p2, -6, -6);
        //a.addRoomHelper(world, p2, -6, 6);
        //a.addRoomHelper(world, p2, 6, -6);
        //a.addRoomHelper(world, p2, 6, 6);
        //a.drawRow(world,p2,Tileset.FLOWER,-5);
        //a.drawColumn(world,p2,Tileset.SAND,-5);
        //a.drawRow(world,p2,Tileset.FLOWER,5);
        //a.addHallways(world, p);
        //a.addHallways(world, p2);
        //a.addRoom(world, p2);
        //Positon p3 = new Positon(6,3);
        //a.addRoom(world, p3);
        ter.renderFrame(world);
    }
}
