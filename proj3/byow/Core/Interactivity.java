package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import edu.princeton.cs.introcs.StdDraw;
import java.awt.*;
import java.io.File;
import java.math.BigInteger;
import java.util.Set;
import static byow.Core.Engine.*;
import static byow.Core.Utils.*;

public class Interactivity {
    private static final int TILE_SIZE = 16;
    private static final int MAX_SIGHT = 3;
    private static final int MIN_SIGHT = 7;
    private int nowSight = 5;
    private boolean sightMode = true;
    private TERenderer ter;
    private TETile[][] world;
    private TETile currentHoverTile = null;
    private WorldGenerator generator;
    private String keysTyped = "";
    private String seed = "";
    private GameState currentState = GameState.MENU;
    private static final Set<Character> NUMBERS =
            Set.of('1', '2', '3', '4', '5', '6', '7', '8', '9', '0');

    public Interactivity(TERenderer ter) {
        this.ter = ter;
    }

    public Interactivity(TERenderer ter, WorldGenerator generator) {
        this.ter = ter;
        this.generator = generator;
    }

    void setKeysTyped(String keysTyped) {
        this.keysTyped = keysTyped;
    }

    void setSeed(String seed) {
        this.seed = seed;
    }

    void setWorld(TETile[][] world) {
        this.world = world;
    }

    public boolean isGameOver() {
        return currentState == GameState.QUIT;
    }

    public TETile[][] getWorld() {
        return this.world;
    }

    public void showTileMessage(TETile tile) {
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.setFont(new Font("Monaco", Font.BOLD, TILE_SIZE));
        StdDraw.textLeft(1, HEIGHT - 0.5, tile.description());
        StdDraw.setFont(new Font("Monaco", Font.BOLD, TILE_SIZE - 2));
    }

    private TETile mouseTETile() {
        int mouseX = (int) StdDraw.mouseX();
        int mouseY = (int) StdDraw.mouseY();
        if (WorldGenerator.inRange(mouseX, mouseY)) {
            return world[mouseX][mouseY];
        }
        return null;
    }

    public void checkMouse() {
        this.currentHoverTile = mouseTETile();
        if (currentHoverTile != null) {
            showTileMessage(currentHoverTile);
        }
    }

    private void moveHelper(char nextKeyTyped, int way) {
        Position avatar = generator.getAvatar();
        Position target = avatar;
        switch (nextKeyTyped) {
            case 'w', 'W' -> target = avatar.shiftPosition(0, 1);
            case 's', 'S' -> target = avatar.shiftPosition(0, -1);
            case 'a', 'A' -> target = avatar.shiftPosition(-1, 0);
            case 'd', 'D' -> target = avatar.shiftPosition(1, 0);
            case 't', 'T' -> sightMode = !sightMode;
            case 'q', 'Q' -> {
                if (keysTyped.charAt(keysTyped.length() - 1) == ':') {
                    currentState = GameState.QUIT;
                    keysTyped = keysTyped.substring(0, keysTyped.length() - 1);
                    saveGame();
                }
                return;
            }
            default -> {
                keysTyped += nextKeyTyped;
                return;
            }
        }
        keysTyped += nextKeyTyped;
        if (WorldGenerator.inRange(target.x(), target.y()) && canChangeAvatar(target) && target != avatar) {
            changeAvatarTEtile(target, way);
        }
    }

    private void saveGame() {
        File CWD = new File(System.getProperty("user.dir"));
        File save = join(CWD, "savefile.txt");
        String total;
        if (keysTyped.startsWith("N") || keysTyped.startsWith("n")) {
            total = keysTyped;
        } else {
            total = "N" + seed + "S" + keysTyped;
        }
        writeContents(save, total);
    }

    public void applyCommands(String commands) {
        int index = 0;
        while (index < commands.length() && currentState != GameState.QUIT) {
            char now = commands.charAt(index);
            this.moveHelper(now, Engine.STRING); // 这里的 this 保证了改的是当前对象的 sightMode
            index += 1;
        }
    }

    public void loadGame() {
        File CWD = new File(System.getProperty("user.dir"));
        File save = join(CWD, "savefile.txt");
        if (!save.exists()) {
            StdDraw.clear(StdDraw.BLACK);
            drawStart();
            StdDraw.setPenColor(StdDraw.YELLOW);
            StdDraw.setFont(new Font("Monaco", Font.PLAIN, 18));
            StdDraw.text((double) WIDTH / 2, (double) HEIGHT / 2 - 6, "No historical games");
            currentState = GameState.MENU;
            return;
        }
        String load = readContentsAsString(save);
        int end = Math.max(load.indexOf('S'), load.indexOf('s'));
        this.seed = load.substring(1, end).replaceAll("[^0-9]", "");
        this.generator = new WorldGenerator(Long.parseLong(this.seed));
        this.world = this.generator.generate();
        this.keysTyped = "n" + this.seed + "s";
        String commands = load.substring(end + 1);
        this.applyCommands(commands);
        this.currentState = GameState.PLAYING;
    }

    public void moveAvatarKeyboard() {
        if (StdDraw.hasNextKeyTyped()) {
            char nextKeyTyped = StdDraw.nextKeyTyped();
            moveHelper(nextKeyTyped, KEYBOARD);
        }
    }

    public TETile[][] moveAvatarString(char input) {
        moveHelper(input, STRING);
        return getWorld();
    }

    private void changeAvatarTEtile(Position p, int way) {
        Position avatar = generator.getAvatar();
        int targetX = p.x();
        int targetY = p.y();
        TETile target = world[targetX][targetY];
        world[targetX][targetY] = Tileset.AVATAR;
        world[avatar.x()][avatar.y()] = target;
        generator.changeAvatar(p);
    }

    private boolean canChangeAvatar(Position p) {
        return world[p.x()][p.y()] != Tileset.NOTHING
                && world[p.x()][p.y()] != Tileset.WALL;
    }

    public enum GameState { MENU, SEED_INPUT, PLAYING, QUIT, LOAD }
    private void clearBuffer() {
        while (StdDraw.hasNextKeyTyped()) {
            StdDraw.nextKeyTyped();
        }
    }

    public void runGameLoop() {
        while (currentState != GameState.QUIT) {
            switch (currentState) {
                case MENU -> menu();
                case SEED_INPUT -> checkSeedInput();
                case PLAYING -> startGame();
                case LOAD -> loadGame();
            }
            StdDraw.pause(10);
        }
        System.out.println("GAME OVER!");
    }

    public void menu() {
        drawStart();
        clearBuffer();
        boolean nextStep = false;
        while (!nextStep) {
            while (!StdDraw.hasNextKeyTyped()) {
                StdDraw.pause(10);
            }
            char nextValidKey = StdDraw.nextKeyTyped();
            switch (nextValidKey) {
                case 'n', 'N' -> {
                    drawSeed("");
                    currentState = GameState.SEED_INPUT;
                    nextStep = true;
                }
                case 'l', 'L' -> {
                    currentState = GameState.LOAD;
                    nextStep = true;
                }
                case 'q', 'Q' -> {
                    currentState = GameState.QUIT;
                    nextStep = true;
                }
            }
        }
    }

    private void drawStart() {
        StdDraw.setPenColor(StdDraw.WHITE);
        StdDraw.setFont(new Font("Monaco", Font.BOLD, 30));
        StdDraw.text((double) WIDTH / 2, (double) HEIGHT * 3 / 4, "CS61B: THE GAME");
        StdDraw.setFont(new Font("Monaco", Font.PLAIN, 18));
        StdDraw.text((double) WIDTH / 2, (double) HEIGHT / 2, "New Game(N)");
        StdDraw.text((double) WIDTH / 2, (double) HEIGHT / 2 - 2, "Load Game(L)");
        StdDraw.text((double) WIDTH / 2, (double) HEIGHT / 2 - 4, "Quit (Q)");
        StdDraw.show();
    }

    private void drawSeed(String seed) {
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(StdDraw.YELLOW);
        StdDraw.setFont(new Font("Monaco", Font.PLAIN, 18));
        StdDraw.text((double) WIDTH / 2, (double) HEIGHT / 2 - 6, "Enter Seed, Confirm with S");
        StdDraw.text((double) WIDTH / 2, (double) HEIGHT / 2 - 8, seed);
        drawStart();
        StdDraw.show();
    }

    public void checkSeedInput() {
        clearBuffer();
        boolean nextStep = false;
        String input = "";
        while (!nextStep) {
            while (!StdDraw.hasNextKeyTyped()) {
                StdDraw.pause(10);
            }
            char nextValidKey = StdDraw.nextKeyTyped();
            if (NUMBERS.contains(nextValidKey)) {
                input += nextValidKey;
                this.seed = input;
                drawSeed(input);
            } else if (nextValidKey == 's' || nextValidKey == 'S') {
                if (checkSeedValid()) {
                    nextStep = true;
                    this.generator = new WorldGenerator(Long.parseLong(this.seed));
                    this.world = generator.generate();
                    currentState = GameState.PLAYING;
                } else {
                    input = "";
                    drawSeed("请输入合理数字");
                }
            } else if (nextValidKey == 'q' || nextValidKey == 'Q') {
                nextStep = true;
                StdDraw.clear(StdDraw.BLACK);
                currentState = GameState.MENU;
            }
        }
    }

    private boolean checkSeedValid() {
        if (seed.isEmpty()) {
            return false;
        }
        BigInteger seedValue = new BigInteger(seed);
        BigInteger maxLong = BigInteger.valueOf(Long.MAX_VALUE);
        return maxLong.compareTo(seedValue) >= 0;
    }

    public void startGame() {
        while (currentState != GameState.QUIT) {
//          System.out.println("x:" + generator.getAvatarPosition().x()  + "y:" +generator.getAvatarPosition().y());
            moveAvatarKeyboard();
//          2. 开始这一帧的绘制
            if (sightMode) {
                drawSigntMode();// 盖上阴影（在缓冲区）
            } else {
                drawTilesOnly();// 画地图（在缓冲区）
            }
            checkMouse();     // 画 HUD 文字（在缓冲区）
            StdDraw.show();
            StdDraw.pause(10);
        }
    }

    private void drawSigntMode() {
        Position p = generator.getAvatar();
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
// 计算当前格子 (x, y) 到小人 (p.x, p.y) 的欧几里得距离
                double dist = Math.sqrt(Math.pow(x - p.x(), 2) + Math.pow(y - p.y(), 2));
                if (dist < nowSight) {
                    world[x][y].draw(x, y);
                } else {
                    Tileset.NOTHING.draw(x, y);
                }
            }
        }
    }

    private void drawTilesOnly() {
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                if (world[x][y] == null) {
                    continue;
                }
// 每一个 Tile 都有自己的 draw 方法
                world[x][y].draw(x, y);
            }
        }
    }

    public static void main(String[] args) {
        TERenderer ter = new TERenderer();
        ter.initialize(80, 30);
        StdDraw.setPenColor(Color.yellow);
        Interactivity temp = new Interactivity(ter);
        Position p = new Position(40,15);
//todo Math.min(p.x() + temp.nowSight, HEIGHT);
        double[] x = { p.x() - temp.nowSight, p.x(), p.x() + temp.nowSight, p.x() };
        double[] y = {p.y(), p.y() + temp.nowSight, p.y(), p.y() - temp.nowSight };
//左上右下
//StdDraw.filledPolygon(x, y);
        StdDraw.setPenColor(Color.WHITE);
        double[] xLeftUp = {0, 0, p.x(), p.x(), p.x() - temp.nowSight};
        double[] yLeftUp = {p.y(), HEIGHT, HEIGHT, p.y() + temp.nowSight, p.y()};
        StdDraw.filledPolygon(xLeftUp, yLeftUp);
        double[] xRightUp = {p.x(), p.x(), WIDTH, WIDTH, p.x() + temp.nowSight};
        double[] yRightUp = {p.y() + temp.nowSight, HEIGHT, HEIGHT, p.y(), p.y()};
        StdDraw.filledPolygon(xRightUp, yRightUp);
        double[] xLeftDown = {0, 0, p.x() - temp.nowSight, p.x(), p.x()};
        double[] yLeftDown = {0, p.y(), p.y(), p.y() - temp.nowSight, 0};
        StdDraw.filledPolygon(xLeftDown, yLeftDown);
        double[] xRightDown = {p.x(), p.x(), p.x() + temp.nowSight, WIDTH, WIDTH};
        double[] yRightDown = {0, p.y() - temp.nowSight, p.y(), p.y(), 0};
        StdDraw.filledPolygon(xRightDown, yRightDown);
        StdDraw.show();
//        Engine engine = new Engine();
//        TETile[][] world =  engine.interactWithInputString("ladds");
//        ter.renderFrame(world);
    }
}