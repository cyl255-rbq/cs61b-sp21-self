package byow.lab13;

import byow.Core.RandomUtils;
import edu.princeton.cs.introcs.StdDraw;

import java.awt.Color;
import java.awt.Font;
import java.util.Random;

public class MemoryGame {
    /** The width of the window of this game. */
    private int width;
    /** The height of the window of this game. */
    private int height;
    /** The current round the user is on. */
    private int round;
    /** The Random object used to randomly generate Strings. */
    private Random rand;
    /** Whether or not the game is over. */
    private boolean gameOver;
    /** Whether or not it is the player's turn. Used in the last section of the
     * spec, 'Helpful UI'. */
    private boolean playerTurn;
    /** The characters we generate random Strings from. */
    private static final char[] CHARACTERS = "abcdefghijklmnopqrstuvwxyz".toCharArray();
    /** Encouraging phrases. Used in the last section of the spec, 'Helpful UI'. */
    private static final String[] ENCOURAGEMENT = {"You can do this!", "I believe in you!",
                                                   "You got this!", "You're a star!", "Go Bears!",
                                                   "Too easy for you!", "Wow, so impressive!"};

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Please enter a seed");
            return;
        }
        long seed = Long.parseLong(args[0]);
        MemoryGame game = new MemoryGame(40, 40, seed);
        game.startGame();
    }



    public MemoryGame(int width, int height, long seed) {
        /* Sets up StdDraw so that it has a width by height grid of 16 by 16 squares as its canvas
         * Also sets up the scale so the top left is (0,0) and the bottom right is (width, height)
         */
        this.width = width;
        this.height = height;
        StdDraw.setCanvasSize(this.width * 16, this.height * 16);
        Font font = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(font);
        StdDraw.setXscale(0, this.width);
        StdDraw.setYscale(0, this.height);
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.enableDoubleBuffering();

        //TODO: Initialize random number generator
        this.rand = new Random(seed);
    }

    public String generateRandomString(int n) {
        if (n < 1) {
            throw new IllegalArgumentException("n must > 0");
        }
        String total = "";
        for (int i = 0; i < n; i += 1) {
            int index = RandomUtils.uniform(this.rand, CHARACTERS.length);
            total += CHARACTERS[index];
        }
        return total;
    }

    public void drawFrame(String s) {
        //TODO: Take the string and display it in the center of the screen
        //TODO: If game is not over, display relevant game information at the top of the screen
        StdDraw.clear(Color.BLACK);
        if (!gameOver) {
            Font ui = new Font("Monaco", Font.BOLD, 20);
            StdDraw.setFont(ui);
            int encouragement = RandomUtils.uniform(rand, ENCOURAGEMENT.length);
            StdDraw.textLeft(0, height - 1, "Round: " + round);
            StdDraw.textRight(width, height - 1, ENCOURAGEMENT[encouragement]);
            StdDraw.line(0, height - 2, width, height - 2);
            if (!playerTurn) {
                StdDraw.text(width / 2, height - 1, "Watch!");
            } else {
                StdDraw.text(width / 2, height - 1, "Type!");
            }
        }
        Font font = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(font);
        StdDraw.text(width / 2, height / 2, s);
        StdDraw.show();
    }

    public void flashSequence(String letters) {
        //TODO: Display each character in letters, making sure to blank the screen between letters
        playerTurn = false;
        int len = letters.length();
        for (int i = 0; i < len; i += 1) {
            drawFrame(letters.substring(i, i+1));
            StdDraw.pause(1000);
            if (i == len - 1) {
                playerTurn = true;
            }
            drawFrame("");
            StdDraw.pause(500);
        }
    }

    public String solicitNCharsInput(int n) {
        //TODO: Read n letters of player input
        while (StdDraw.hasNextKeyTyped()) {
            StdDraw.nextKeyTyped();
        }
        String input = "";
        for (int i = 0; i < n; i += 1) {
            while (!StdDraw.hasNextKeyTyped()) {
                StdDraw.pause(10);
            }
            input += StdDraw.nextKeyTyped();
            drawFrame(input);
        }
        return input;
    }

    public void startGame() {
        //TODO: Set any relevant variables before the game starts
        this.round = 1;
        this.gameOver = false;
        //TODO: Establish Engine loop
        while (!gameOver) {
            this.playerTurn = false;
            drawFrame("Round: " + round);
            StdDraw.pause(1000);
            String system = generateRandomString(round);
            flashSequence(system);
            String user = solicitNCharsInput(round);
            if (!user.equals(system)) {
                playerTurn = false;
                gameOver = true;
                drawFrame("Game Over! You made it to round:" + round);
            }
            round += 1;
        }
    }

}
