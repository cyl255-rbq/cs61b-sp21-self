package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import edu.princeton.cs.introcs.StdDraw;

import java.awt.*;
import java.io.File;
import java.util.Set;

import static byow.Core.Engine.*;
import static byow.Core.Utils.*;



public class Interactivity {
    private static final int TILE_SIZE = 16;
    private boolean gameOver = false;
    private TETile before = null;
    private TERenderer ter;
    private TETile[][] world;
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

    public void setSeed(String seed) {
        this.seed = seed;
    }

    public void setWorld(TETile[][] world) {
        this.world = world;
    }

    public boolean isGameOver() {
        return this.gameOver;
    }

    public TETile[][] getWorld() {
        return this.world;
    }

    public void showTileMessage(TETile tile) {
        ter.renderFrame(world);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.setFont(new Font("Monaco", Font.BOLD, TILE_SIZE));
        StdDraw.textLeft(1, HEIGHT - 0.5, tile.description());
        StdDraw.show();
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
        TETile now = mouseTETile();
        if (now != null && !now.equals(before)) {
            showTileMessage(now);
            before = now;
        }
    }

    private void moveHelper(char nextKeyTyped, int way) {
        Position avatar = generator.getAvatar();
        Position target;
        switch (nextKeyTyped) {
            case 'w', 'W' -> target = avatar.shiftPosition(0, 1);
            case 's', 'S' -> target = avatar.shiftPosition(0, -1);
            case 'a', 'A' -> target = avatar.shiftPosition(-1, 0);
            case 'd', 'D' -> target = avatar.shiftPosition(1, 0);
            case 'Q' -> {
                if (keysTyped.charAt(keysTyped.length() - 1) == ':') {
                    gameOver = true;
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
        if (WorldGenerator.inRange(target.x(), target.y()) && canChangeAvatar(target)) {
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
        this.keysTyped = load;
        int end = Math.max(load.indexOf('S'), load.indexOf('s'));
        this.seed = load.substring(1, end).replaceAll("[^0-9]", "");
        Engine temp = new Engine();
        this.world = temp.interactWithInputString(load);
        this.generator = temp.getGenerator();
        currentState = GameState.PLAYING;
    }

    public void moveAvatarKeyboard() {
        if (StdDraw.hasNextKeyTyped()) {
            char nextKeyTyped = StdDraw.nextKeyTyped();
            moveHelper(nextKeyTyped, KEYBOARD);
        }
    }

    public char getMoveNextKey(String input, int index) {
        char returnChar = input.charAt(index);
        return returnChar;
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
        if (way == Engine.KEYBOARD) {
            TETile mouseNow = mouseTETile();
            ter.renderFrame(world);
            if (mouseNow != null) {
                showTileMessage(mouseNow);
            }
        }
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
        return !seed.isEmpty() && seed.length() < 19;
    }

    public void startGame() {
        StdDraw.setFont(new Font("Monaco", Font.BOLD, TILE_SIZE - 2));
        ter.renderFrame(world);
        while (!gameOver) {
            moveAvatarKeyboard();
            checkMouse();
            StdDraw.pause(10);
        }
    }

    public static void main(String[] args) {
        TERenderer ter = new TERenderer();
        ter.initialize(80, 30);
        Interactivity b = new Interactivity(ter);
        b.runGameLoop();

    }
}
