package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import edu.princeton.cs.introcs.StdDraw;
import java.awt.*;
import java.io.File;
import java.math.BigInteger;
import java.util.*;
import java.util.List;

import static byow.Core.Engine.*;
import static byow.Core.PathNode.manhattan;
import static byow.Core.Utils.*;
import static byow.Core.WorldGenerator.couldBeNeighbour;

public class Interactivity {
    private final double ZOOM = 0.8;
    private static final int TILE_SIZE = 16;
    private static final int MAX_SIGHT = 10;
    private static final int MIN_SIGHT = 5;
    private final int MAX_TICKS = 6000;
    private int totalTicks = 0;
    private int nowSight = 5;
    private boolean sightMode = true;
    private boolean showEnemyPath = false;
    private boolean savingGame = false;
    private TETile[][] world;
    private TETile currentMouseTile = null;
    private WorldGenerator generator;
    private String keysTyped = "";
    private String seed = "";
    private char lastKey = '.';
    private Random random;
    TETile enemyTile = Tileset.FLOWER;
    List<Enemy> enemies = new ArrayList<>();
    private GameState currentState = GameState.MENU;
    private static final Set<Character> NUMBERS =
            Set.of('1', '2', '3', '4', '5', '6', '7', '8', '9', '0');
    private ViewMode currentView = ViewMode.TOP_DOWN;

    public Interactivity() {
    }

    public Interactivity(WorldGenerator generator) {
        this.generator = generator;
        this.random = new Random(generator.getSeed());
    }

    public enum ViewMode { TOP_DOWN, ISOMETRIC}

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
        StdDraw.setXscale(0, WIDTH);
        StdDraw.setYscale(0, HEIGHT);
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
        this.currentMouseTile = mouseTETile();
        if (currentMouseTile != null) {
            showTileMessage(currentMouseTile);
        }
    }

    private void moveHelper(char nextKeyTyped) {
        Position avatar = generator.getAvatar();
        Position target = avatar;
        switch (nextKeyTyped) {
            case 'w', 'W' -> {
                target = avatar.shiftPosition(0, 1);
                lastKey = 'w';
            }
            case 's', 'S' -> {
                target = avatar.shiftPosition(0, -1);
                lastKey = 's';
            }
            case 'a', 'A' -> {
                target = avatar.shiftPosition(-1, 0);
                lastKey = 'a';
            }
            case 'd', 'D' -> {
                target = avatar.shiftPosition(1, 0);
                lastKey = 'd';
            }
            case 't', 'T' -> {
                sightMode = !sightMode;
                lastKey = 't';
            }
            case 'e', 'E' -> {
                showEnemyPath = !showEnemyPath;
                lastKey = 'e';
            }
            case 'v', 'V' -> {
                if (currentView == ViewMode.TOP_DOWN) {
                    currentView = ViewMode.ISOMETRIC;
                } else {
                    currentView = ViewMode.TOP_DOWN;
                }
                lastKey = 'e';
            }
            case ':' -> lastKey = ':';
            case 'q', 'Q' -> {
                if (lastKey == ':') {
                    saveGame();
                    savingGame = true;
                    currentState = GameState.MENU;
                }
                return;
            }
            default -> {
                lastKey = nextKeyTyped;
                keysTyped += nextKeyTyped;
                return;
            }
        }
        keysTyped += nextKeyTyped;
        if (WorldGenerator.inRange(target.x(), target.y()) && canChangeAvatar(target) && target != avatar) {
            for (Enemy enemy : enemies) {
                if (avatar.equals(enemy.position())) {
                    world[avatar.x()][avatar.y()] = Tileset.FLOOR;
                    currentState = GameState.QUIT;
                    return;
                }
            }
            changeAvatarTEtile(target);
            updateAllPath(generator.getAvatar());
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
            enemyLogic();
            this.moveHelper(now);
            totalTicks += 1;
            index += 1;
        }
        totalTicks += 1;
    }

    private void clearSet() {
        this.nowSight = 5;
        this.totalTicks = 0;
        this.enemies.clear();
        this.showEnemyPath = false;
        this.savingGame = false;
        this.sightMode = true;
        this.currentMouseTile = null;
        this.lastKey = '.';
        this.keysTyped = "";
        this.currentView = ViewMode.TOP_DOWN;
    }

    public void loadGame() {
        clearSet();
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
        int end = getIndex(load);
        this.seed = load.substring(1, end).replaceAll("[^0-9]", "");
        this.generator = new WorldGenerator(Long.parseLong(this.seed));
        this.world = this.generator.generate();
        this.random = new Random(Long.parseLong(this.seed));
        this.keysTyped = "n" + this.seed + "s";
        String commands = load.substring(end + 1);
        this.applyCommands(commands);
        this.currentState = GameState.PLAYING;
    }

    public void moveAvatarKeyboard() {
        if (StdDraw.hasNextKeyTyped()) {
            char nextKeyTyped = StdDraw.nextKeyTyped();
            moveHelper(nextKeyTyped);
        } else {
            keysTyped += ".";
        }
    }

    public TETile[][] moveAvatarString(char input) {
        moveHelper(input);
        return getWorld();
    }

    private void changeAvatarTEtile(Position target) {
        changeTEtile(generator.getAvatar(), target);
        generator.changeAvatar(target);
    }

    private void changeTEtile(Position start, Position target) {
        TETile temp = world[target.x()][target.y()];
        world[target.x()][target.y()] = world[start.x()][start.y()];
        world[start.x()][start.y()] = temp;
    }

    private boolean canChangeAvatar(Position p) {
        return world[p.x()][p.y()] != Tileset.NOTHING
                && world[p.x()][p.y()] != Tileset.WALL;
    }

    public enum GameState { MENU, SEED_INPUT, PLAYING, QUIT, LOAD}
    private void clearBuffer() {
        while (StdDraw.hasNextKeyTyped()) {
            StdDraw.nextKeyTyped();
        }
    }

    private HashSet<Position> getNeighbours(Position curr) {
        HashSet<Position> neighbours = new HashSet<>();
        HashSet<Position> around = new HashSet<>();
        around.add(curr.shiftPosition(0,1));
        around.add( curr.shiftPosition(0, -1));
        around.add(curr.shiftPosition(-1, 0));
        around.add(curr.shiftPosition(1, 0));
        for (Position p : around) {
            if (couldBeNeighbour(world,  p)) {
                neighbours.add(p);
            }
        }
        return neighbours;
    }

    public List<Position> recursivePath(HashMap<Position, Position> edgeTo, Position target) {
        List<Position> path = new ArrayList<>();
        while (target != null) {
            path.add(target);
            target = edgeTo.get(target);
        }
        Collections.reverse(path);
        return path;
    }

    public List<Position> getPath(Position start, Position target) {
        PriorityQueue<PathNode> fringe = new PriorityQueue<>();
        HashMap<Position, Position> edgeTo = new HashMap<>();
        HashMap<Position, Integer> distTo = new HashMap<>();
        fringe.add(new PathNode(start, 0, manhattan(start, target)));
        distTo.put(start, 0);
        while (!fringe.isEmpty()) {
            PathNode curr = fringe.poll();
            if (curr.position().equals(target)) {
                return recursivePath(edgeTo, target);
            }
            for (Position p : getNeighbours(curr.position())) {
                int newDist = curr.distTo() + 1;
                if (newDist < distTo.getOrDefault(p, Integer.MAX_VALUE)) {
                    edgeTo.put(p, curr.position());
                    distTo.put(p, newDist);
                    fringe.add(new PathNode(p, newDist, manhattan(p, target)));
                }
            }
        }
        return null;
    }

    public void runGameLoop() {
        while (currentState != GameState.QUIT) {
            switch (currentState) {
                case MENU -> menu();
                case SEED_INPUT -> checkSeedInput();
                case PLAYING -> startGame();
                case LOAD -> loadGame();
            }
        }
    }

    private void drawSettlementPage(String state) {
        StdDraw.setXscale(0, WIDTH);
        StdDraw.setYscale(0, HEIGHT);
        StdDraw.setFont(new Font("Monaco", Font.BOLD, 5 * TILE_SIZE));
        if (state.equals("WIN")) {
            StdDraw.setPenColor(Color.GREEN);
            StdDraw.text((double) WIDTH / 2, (double) HEIGHT / 2, "Well done, you won!");
        } else if (state.equals("LOSE")) {
            StdDraw.setPenColor(Color.RED);
            StdDraw.text((double) WIDTH / 2, (double) HEIGHT / 2, "Try again, you lose.");
        } else if (state.equals("SAVE")) {
            StdDraw.setPenColor(Color.white);
            StdDraw.text((double) WIDTH / 2, (double) HEIGHT / 2, "Saved successfully");
        }
        StdDraw.show();
    }

    public void menu() {
        StdDraw.clear(StdDraw.BLACK);
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
        StdDraw.text((double) WIDTH / 2, (double) HEIGHT * 3 / 4, "THE GAME");//CS61B:
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
                    this.random = new Random(Long.parseLong(this.seed));
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

    public void updateAllPath(Position target) {
        for (Enemy enemy : enemies) {
            enemy.updatePath(getPath(enemy.position(), target));
        }
    }

    private void spawnEnemy() {
        Position p = generator.getAvatar();
        int attempts = 0;
        while (attempts < 100) {
            int x = RandomUtils.uniform(random, 1, WIDTH - 1);
            int y = RandomUtils.uniform(random, 1, HEIGHT - 1);
            double dist = Math.sqrt(Math.pow(x - p.x(), 2) + Math.pow(y - p.y(), 2));
            if (dist >= 8 && world[x][y] == Tileset.FLOOR && world[x][y] != enemyTile) {
                world[x][y] = enemyTile;
                Position enemy = new Position(x, y);
                enemies.add(new Enemy(enemy, getPath(enemy, generator.getAvatar())));
                break;
            }
            attempts += 1;
        }
    }

    private void moveAllEnemies() {
        double dist = WIDTH + HEIGHT;
        for (Enemy enemy : enemies) {
            double enemyDist;
            List<Position> path = enemy.path();
            if (path == null || path.isEmpty()) continue;
            if (path.getFirst().equals(enemy.position())) {
                path.removeFirst();
            }
            if (path.isEmpty()) continue;
            Position enemyPosition = enemy.position();
            Position nextMove = path.getFirst();
            if (world[nextMove.x()][nextMove.y()] == world[enemyPosition.x()][enemyPosition.y()]) {
                continue;
            }
            Position avatar = generator.getAvatar();
            if (nextMove.equals(avatar)) {
                world[nextMove.x()][nextMove.y()] = world[enemyPosition.x()][enemyPosition.y()];
                world[enemyPosition.x()][enemyPosition.y()] = Tileset.FLOOR;
                currentState = GameState.QUIT;
                return;
            }
            changeTEtile(enemyPosition, nextMove);
            enemy.updatePosition(nextMove);
            path.removeFirst();
            Position newPosition = enemy.position();
            enemyDist = Math.sqrt(Math.pow(avatar.x() - newPosition.x(), 2)
                    + Math.pow(avatar.y() - newPosition.y(), 2));
            if (enemyDist < dist) {
                dist = enemyDist;
            }
        }
        int sight = (int) (15 - dist);
        if (sight < MIN_SIGHT) {
            nowSight = MIN_SIGHT;
        } else if (sight > MAX_SIGHT) {
            nowSight = MAX_SIGHT;
        } else {
            nowSight = sight;
        }
    }

    private void enemyLogic() {
        if (totalTicks >= MAX_TICKS) {
            currentState = GameState.QUIT;
        }
        if (totalTicks % 1000 == 0) {
            spawnEnemy();
        }
        if (totalTicks % 50 == 0) {
            moveAllEnemies();
        }
    }

    private void drawEnemyPath() {
        temporaryShowPath();
        if (showEnemyPath) {
            for (Enemy enemy : enemies) {
                StdDraw.setPenColor(Color.green);
                for (Position p : enemy.path()) {
                    if (!p.equals(generator.getAvatar()) && !p.equals(enemy.position())) {
                        if (currentView == ViewMode.TOP_DOWN) {
                            StdDraw.filledCircle(p.x() + 0.5, p.y() + 0.5, 0.15);
                        } else {
                            double screenX = getIsoX(p.x(), p.y());
                            double screenY = getIsoY(p.x(), p.y());
                            StdDraw.filledEllipse(screenX, screenY, 0.2, 0.1);
                        }

                    }
                }
            }
        }
    }

    private void temporaryShowPath() {
        if (totalTicks % 100 == 0) {
            this.showEnemyPath = !showEnemyPath;
        }
    }

    private void drawWorld() {
        switch (currentView) {
            case TOP_DOWN -> drawTopDownWorld();
            case ISOMETRIC -> drawIsometricWorld();
        }
    }

    private double getIsoX(int x, int y) {
        return (x - y) * ZOOM - (double) WIDTH / 6;
    }

    private double getIsoY(int x, int y) {
        return (x + y) * ZOOM / 2 - (double) HEIGHT * 3 / 5;
    }

    private void drawTopDownWorld() {
        StdDraw.clear(Color.BLACK);
        StdDraw.setXscale(0, WIDTH);
        StdDraw.setYscale(0, HEIGHT);
        for (int x = 0; x < WIDTH; x += 1) {
            for (int y = 0; y < HEIGHT; y += 1) {
                if (sightMode) {
                    Position avatar = generator.getAvatar();
                    double dist = Math.sqrt(Math.pow(x - avatar.x(), 2) + Math.pow(y - avatar.y(), 2));
                    if (dist >= nowSight) {
                        Tileset.NOTHING.draw(x, y);
                        continue;
                    }
                }
                world[x][y].draw(x, y);
            }
        }
    }

    private void drawIsometricWorld() {
        StdDraw.clear(Color.BLACK);
        StdDraw.setXscale(-WIDTH * ZOOM, WIDTH * ZOOM);
        StdDraw.setYscale(-HEIGHT * ZOOM, HEIGHT * ZOOM);
        for (int x = 0; x < WIDTH; x += 1) {
            for (int y = HEIGHT - 1; y >= 0; y -= 1) {
                double screenX = getIsoX(x, y);
                double screenY = getIsoY(x, y);
                if (sightMode) {
                    Position avatar = generator.getAvatar();
                    double dist = Math.sqrt(Math.pow(x - avatar.x(), 2) + Math.pow(y - avatar.y(), 2));
                    if (dist >= nowSight) {
                        Tileset.NOTHING.draw(screenX, screenY);
                        continue;
                    }
                }
                TETile tile = world[x][y];
                tile.draw(screenX, screenY);
            }
        }
    }

    public void startGame() {
        while (currentState == GameState.PLAYING) {
            enemyLogic();
            moveAvatarKeyboard();
            drawWorld();
            drawEnemyPath();
            checkMouse();
            drawTime();
            StdDraw.show();
            StdDraw.pause(10);
            totalTicks += 1;
        }
        if (savingGame) {
            drawSettlementPage("SAVE");
            StdDraw.pause(1500);
        } else {
            if (totalTicks >= MAX_TICKS) {
                drawSettlementPage("WIN");
            } else {
                drawSettlementPage("LOSE");
            }
            StdDraw.pause(3000);
            currentState = GameState.QUIT;
        }
    }

    private void drawTime() {
        StdDraw.setXscale(0, WIDTH);
        StdDraw.setYscale(0, HEIGHT);
        int time = (MAX_TICKS - totalTicks) / 100;
        StdDraw.setPenColor(Color.YELLOW);
        StdDraw.setFont(new Font("Monaco", Font.PLAIN, 2 * TILE_SIZE));
        StdDraw.textRight(WIDTH / 2, HEIGHT - 1, Integer.toString(time));
        StdDraw.setFont(new Font("Monaco", Font.BOLD, TILE_SIZE - 2));
    }

    public static void main(String[] args) {
        TERenderer ter = new TERenderer();
        ter.initialize(80, 30);
        StdDraw.setPenColor(Color.yellow);
        Interactivity temp = new Interactivity();
        Position p = new Position(40,15);
//        Engine engine = new Engine();
//        TETile[][] world =  engine.interactWithInputString("ladds");
//        ter.renderFrame(world);
    }
}