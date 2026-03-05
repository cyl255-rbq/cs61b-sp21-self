package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;

public class Engine {
    TERenderer ter = new TERenderer();
    /* Feel free to change the width and height. */
    public static final int WIDTH = 80;
    public static final int HEIGHT = 30;

    /**
     * Method used for exploring a fre
     * sh world. This method should handle all inputs,
     * including inputs from the main menu.
     */
    public void interactWithKeyboard() {
    }

    /**
     * Method used for autograding and testing your code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The engine should
     * behave exactly as if the user typed these characters into the engine using
     * interactWithKeyboard.
     *
     * Recall that strings ending in ":q" should cause the game to quite save. For example,
     * if we do interactWithInputString("n123sss:q"), we expect the game to run the first
     * 7 commands (n123sss) and then quit and save. If we then do
     * interactWithInputString("l"), we should be back in the exact same state.
     *
     * In other words, both of these calls:
     *   - interactWithInputString("n123sss:q")
     *   - interactWithInputString("lww")
     *
     * should yield the exact same world state as:
     *   - interactWithInputString("n123sssww")
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */

    public TETile[][] interactWithInputString(String input) {
        // TODO: Fill out this method so that it run the engine using the input
        // passed in as an argument, and return a 2D tile representation of the
        // world that would have been drawn if the same inputs had been given
        // to interactWithKeyboard().
        //
        // See proj3.byow.InputDemo for a demo of how you can make a nice clean interface
        // that works for many different input types.

        //WorldGenerator world = new WorldGenerator(WIDTH, HEIGHT, input);
        WorldGenerator world = getWorldGenerator(input);
        TETile[][] finalWorldFrame = world.generate();
        return finalWorldFrame;
    }

    private static WorldGenerator getWorldGenerator(String input) {
        if (input == null || input.isEmpty()) {
            throw new IllegalArgumentException("输入不能为空");
        }
        if (input.charAt(0) != 'N' && input.charAt(0) != 'n') {
            throw new IllegalArgumentException("第一个字符必须是 'N' 或 'n'");
        }
        int end = Math.max(input.indexOf('S'), input.indexOf('s'));
        if (end == -1) {
            throw new IllegalArgumentException("没有结束符号");
        }
        String seedStr = input.substring(1, end).replaceAll("[^0-9]", "");
        long seed = Long.parseLong(seedStr);
        WorldGenerator world = new WorldGenerator(WIDTH, HEIGHT, seed);
        return world;
    }

//    public static void main(String[] args) {
//        Engine engine = new Engine();
//        String a = "n3312agreS";
//        engine.interactWithInputString(a);
//    }

}
