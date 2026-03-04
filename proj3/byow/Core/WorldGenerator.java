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
    private boolean leftbound = false;
    private boolean rightbound = false;

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
        int heightSign = Integer.signum(height);
        for (int i = 0; i < Math.abs(height); i += 1) {
            drawRow(tiles, p, Tileset.FLOOR, width);
            p = p.shiftPosition(0, heightSign);
        }
    }

    private boolean outEdges(Positon p, int width, int height) {
        return ((p.x() + width > WIDTH - 1) || (p.x() + width < 0)) ||
                ((p.y() + height > HEIGHT - 1) || (p.y() + height < 0)) ||
                (p.x() == 0 || p.y() == 0) || (p.x() == WIDTH -1)
                || (p.y() == HEIGHT-1);
    }

    private boolean overlayInside(TETile[][] tiles, Positon p, int width, int height) {
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

    private boolean inRoomRange(Rectangle room, int x, int y) {
        Positon p = room.positon();
        int width = room.width();
        int height = room.height();
        int xRange = p.x() + width;
        int yRange = p.y() + height;
        boolean xInRange = false;
        boolean yInRange = false;
        if (width > 0) {
            if (p.x() <= x && x < xRange) {
                xInRange = true;
            }
        } else {
            if (xRange < x && x <= p.x()) {
                xInRange = true;
            }
        }
        if (height > 0) {
            if (p.y() <= y && y < yRange) {
                yInRange = true;
            }
        } else {
            if (yRange < y && y <= p.y()) {
                yInRange = true;
            }
        }
        return xInRange && yInRange;
    }

    private boolean overlayOutside(TETile[][] tiles, Positon p, Rectangle father, int width, int height) {
        int widthSign = Integer.signum(width);
        int heightSign = Integer.signum(height);
        p = p.shiftPosition(-widthSign, -heightSign);
        for (int i = 0; i < Math.abs(width) + 1; i += 1) {
            int x = p.x() + i * widthSign;
            int y = p.y() + height + heightSign;
            if (inRoomRange(father, x, y) || inRoomRange(father, x, p.y())) {
                continue;
            }
            if (tiles[x][y] != Tileset.NOTHING || tiles[x][p.y()] != Tileset.NOTHING) {
                return true;
            }
        }
        for (int i = 0; i < Math.abs(height) + 1; i += 1) {
            int y = p.y() + i * heightSign;
            int x = p.x() + width + widthSign;
            if (inRoomRange(father, x, y) || inRoomRange(father, p.x(), y)) {
                continue;
            }
            if (tiles[x][y] != Tileset.NOTHING || tiles[p.x()][y] != Tileset.NOTHING) {
                return true;
            }
        }
        return false;
    }

    public boolean collision(TETile[][] tiles, Positon p, Rectangle father, int width, int height) {
        return outEdges(p, width, height) || overlayInside(tiles, p, width, height)
                || overlayOutside(tiles, p, father, width, height);
    }

    public void addRoomHelper(TETile[][] tiles, Positon p, int width, int height) {
        if (Math.abs(width) < 1 || Math.abs(height) < 1) {
            return;
        }
        fillWithFloor(tiles, p, width, height);
        int bound = p.x() + width;
        if (bound > WIDTH - 3) {
            this.rightbound = true;
        } else if (bound < 3) {
            this.leftbound = true;
        }
    }

    public void addRoomAssist(TETile[][] tiles, Positon p, Rectangle room, int widthSign, int heightSign) {
        int width = RandomUtils.uniform(random, 2, 6) * widthSign;
        int height = RandomUtils.uniform(random, 2, 6) * heightSign;
        if (collision(tiles, p, room, width, height)) {
            return;
        }
        addRoomHelper(tiles, p, width, height);
        alternative.add(new Rectangle(p, width, height));
    }

    public void addHallwaysAssist(TETile[][] tiles, Positon p, Rectangle room, int lengthSign, int anotherSign) {
        int anotherLen = RandomUtils.uniform(random, 1, 2) * anotherSign;
        int length = RandomUtils.uniform(random, 4, 9) * lengthSign;
        int width;
        int height;
        if (RandomUtils.bernoulli(random)) {
            width = length;
            height = anotherLen;
        } else {
            width = anotherLen;
            height = length;
        }
        if (collision(tiles, p, room, width, height)) {
            return;
        }
        addRoomHelper(tiles, p, width, height);
        alternative.add(new Rectangle(p, width, height));
    }

    public void addRectangle(TETile[][] tiles, Positon p,Rectangle room, int widthSign, int heightSign) {
        if (RandomUtils.bernoulli(random)) {
            addRoomAssist(tiles, p, room, widthSign, heightSign);
        } else {
            addHallwaysAssist(tiles, p, room, widthSign, heightSign);
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
    
    private void addNeighbour(TETile[][] tiles, Rectangle room, Direction dir) {
        Positon p = room.positon();
        int width = room.width();
        int height = room.height();
        int widthSign = Integer.signum(width);
        int heightSign = Integer.signum(height);
        int randomSign = RandomUtils.bernoulli(random) ? 1 : -1;
        int xRange = randomNeighbourX(room);
        int yRange = randomNeighbourY(room);
        switch (dir) {
            case UP -> {
                if (heightSign > 0) {
                    p = p.shiftPosition(xRange, height);
                } else {
                    p = p.shiftPosition(xRange, 1);
                }
                addRectangle(tiles, p, room, randomSign, 1);
            }
            case DOWN -> {
                if (heightSign < 0) {
                    p = p.shiftPosition(xRange, height);
                } else {
                    p = p.shiftPosition(xRange, -1);
                }
                addRectangle(tiles, p, room, randomSign, -1);
            }
            case LEFT -> {
                if (widthSign < 0) {
                    p = p.shiftPosition(width, yRange);
                } else {
                    p = p.shiftPosition(-1, yRange);
                }
                addRectangle(tiles, p, room, -1, randomSign);
            }
            case RIGHT -> {
                if (widthSign > 0) {
                    p = p.shiftPosition(width, yRange);
                } else {
                    p = p.shiftPosition(1, yRange);
                }
                addRectangle(tiles, p, room, 1, randomSign);
            }
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
        double probability = 0.5;
        for (Direction d : Direction.values()) {
            switch (d) {
                case LEFT, RIGHT -> {
                    if (RandomUtils.bernoulli(random, probability)) {
                        addNeighbour(tiles, room, d);
                    }
                }
                case UP, DOWN -> {
                    if (RandomUtils.bernoulli(random, 1 - probability)) {
                        addNeighbour(tiles, room, d);
                    }
                }
            }
        }
    }

    private Rectangle randomRectangle() {
        int size = alternative.size();
        int index = RandomUtils.uniform(random, 0, size);
        return alternative.get(index);
    }

    public void generate(TETile[][] tiles) {
        while ((!this.leftbound || !this.rightbound) ) {//&& alternative.size() < (WIDTH * HEIGHT / 20)
            extendRectangle(tiles, randomRectangle());
        }
    }

    public TETile[][] generate() {
        //todo
        return world;
    }


    public static void main(String[] args) {
        TERenderer ter = new TERenderer();
        ter.initialize(80, 30);
        TETile[][] world = new TETile[80][30];
        WorldGenerator a = new WorldGenerator(80,30,1232223);
        a.fillWithNothing(world);
        //Positon p = new Positon(80, 30);
        Positon p0 =new Positon(0,0);
        Positon p1 = new Positon(20,15);
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

        a.addRoomHelper(world,p2, -3, -3);
        a.alternative.add(new Rectangle(p2, -3, -3));
        a.generate(world);
        a.fillWall(world);
        ter.renderFrame(world);
    }
}
