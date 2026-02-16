package byow.lab12;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.Random;

/**
 * Draws a world consisting of hexagonal regions.
 */
public class HexWorld {
    private static final int WIDTH = 50;
    private static final int HEIGHT = 50;

    private static final long SEED = 2873123;
    private static final Random RANDOM = new Random(SEED);

    private static class Positon {
        int x;
        int y;

        Positon(int x, int y) {
            this.x = x;
            this.y = y;
        }

        private Positon shiftPosition(int x, int y) {
            return new Positon(this.x + x, this.y + y);
        }

        private Positon shiftBottom(int size) {
            return this.shiftPosition(0, -2 * size);
        }

        private Positon shiftRightUp(int size) {
            return this.shiftPosition(2 * size - 1, size);
        }

        private Positon shiftRightDown(int size) {
            return this.shiftPosition(2 * size - 1, -size);
        }

        private Positon shiftLeftDown(int size) {
            return this.shiftPosition(-2 * size + 1, -size);
        }

    }



    /**
     * Fills the given 2D array of tiles with RANDOM tiles.
     * @param tiles
     */
    public static void fillWithNothing(TETile[][] tiles) {
        int height = tiles[0].length;
        int width = tiles.length;
        for (int x = 0; x < width; x += 1) {
            for (int y = 0; y < height; y += 1) {
                tiles[x][y] = Tileset.NOTHING;
            }
        }
    }

    /** Picks a RANDOM tile with a 33% change of being
     *  a wall, 33% chance of being a flower, and 33%
     *  chance of being empty space.
     */
    private static TETile randomTile() {
        int tileNum = RANDOM.nextInt(5);
        switch (tileNum) {
            case 0: return Tileset.GRASS;
            case 1: return Tileset.FLOWER;
            case 2: return Tileset.SAND;
            case 3: return Tileset.MOUNTAIN;
            case 4: return Tileset.TREE;
            default: return Tileset.NOTHING;
        }
    }

    private static void drawRow(TETile[][] tiles, Positon p, TETile tile, int length) {
        for (int i = 0; i < length; i += 1) {
            tiles[p.x + i][p.y] = tile;
        }
    }

    public static void addHexagonHelper(TETile[][] tiles, Positon p, TETile tile, int size, int times) {
        if (times > 0) {
            drawRow(tiles, p, tile, size);
            addHexagonHelper(tiles, p.shiftPosition(-1, -1), tile, size + 2, times - 1);
            drawRow(tiles, p.shiftPosition(0, -2 * times + 1), tile, size);
        }
    }

    public static void addHexagon(TETile[][] tiles, Positon p, int size) {
        if (size < 2) {
            return;
        }
        addHexagonHelper(tiles, p, randomTile(), size, size);
    }

    public static void addHexagonColumn(TETile[][] tiles, Positon p, int size, int num) {
        for (int i = 0; i < num; i += 1) {
            addHexagon(tiles, p, size);
            p = p.shiftBottom(size);
        }
//        if (num > 0) {
//            addHexagon(tiles, p, size);
//            addHexagonColumn(tiles, p.shiftBottom(size), size, num - 1);
//        }
    }

    public static void addHexagonTessellation(TETile[][] tiles, Positon p, int size, int tessSize) {
        for (int i = 0; i < tessSize ; i += 1) {
            addHexagonColumn(tiles, p, size, tessSize + i);
            p = p.shiftRightUp(size);
        }
        p = p.shiftLeftDown(size);
        for (int i = 1; tessSize - i > 0 ; i += 1) {
            p = p.shiftRightDown(size);
            addHexagonColumn(tiles, p, size, 2 * tessSize -1 - i);
        }
    }

//    public static void drawtest(TETile[][] tiles) {
//        fillWithNothing(tiles);
//        Positon p = new Positon(5, 25);
//        addHexagonTessellation(tiles, p, 2, 4);
//        addHexagonColumn(tiles, p.shiftRightUp(3), 3, 3);
//        addHexagonColumn(tiles, p, 3, 3);
//        addHexagon(tiles, p, 3);
//        addHexagon(tiles, p.shiftRightUp(3), 3);
//        addHexagon(tiles, p.shiftRightUp(3).shiftRightDown(3), 3);
//        drawRow(tiles, p, Tileset.WALL, 5);
//    }

    public static void main(String[] args) {
        TERenderer ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);
        TETile[][] world = new TETile[WIDTH][HEIGHT];
        fillWithNothing(world);
        Positon p = new Positon(5, 40);
        addHexagonTessellation(world, p, 3, 4);
        ter.renderFrame(world);
    }


}
