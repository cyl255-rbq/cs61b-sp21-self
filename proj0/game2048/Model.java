package game2048;

import java.util.Formatter;
import java.util.Observable;


/** The state of a game of 2048.
 *  @author TODO: YOUR NAME HERE
 */
public class Model extends Observable {
    /** Current contents of the board. */
    private Board board;
    /** Current score. */
    private int score;
    /** Maximum score so far.  Updated when game ends. */
    private int maxScore;
    /** True iff game is ended. */
    private boolean gameOver;

    /* Coordinate System: column C, row R of the board (where row 0,
     * column 0 is the lower-left corner of the board) will correspond
     * to board.tile(c, r).  Be careful! It works like (x, y) coordinates.
     */

    /** Largest piece value. */
    public static final int MAX_PIECE = 2048;

    /** A new 2048 game on a board of size SIZE with no pieces
     *  and score 0. */
    public Model(int size) {
        board = new Board(size);
        score = maxScore = 0;
        gameOver = false;
    }

    /** A new 2048 game where RAWVALUES contain the values of the tiles
     * (0 if null). VALUES is indexed by (row, col) with (0, 0) corresponding
     * to the bottom-left corner. Used for testing purposes. */
    public Model(int[][] rawValues, int score, int maxScore, boolean gameOver) {
        int size = rawValues.length;
        board = new Board(rawValues, score);
        this.score = score;
        this.maxScore = maxScore;
        this.gameOver = gameOver;
    }

    /** Return the current Tile at (COL, ROW), where 0 <= ROW < size(),
     *  0 <= COL < size(). Returns null if there is no tile there.
     *  Used for testing. Should be deprecated and removed.
     *  */
    public Tile tile(int col, int row) {
        return board.tile(col, row);
    }

    /** Return the number of squares on one side of the board.
     *  Used for testing. Should be deprecated and removed. */
    public int size() {
        return board.size();
    }

    /** Return true iff the game is over (there are no moves, or
     *  there is a tile with value 2048 on the board). */
    public boolean gameOver() {
        checkGameOver();
        if (gameOver) {
            maxScore = Math.max(score, maxScore);
        }
        return gameOver;
    }

    /** Return the current score. */
    public int score() {
        return score;
    }

    /** Return the current maximum game score (updated at end of game). */
    public int maxScore() {
        return maxScore;
    }

    /** Clear the board to empty and reset the score. */
    public void clear() {
        score = 0;
        gameOver = false;
        board.clear();
        setChanged();
    }

    /** Add TILE to the board. There must be no Tile currently at the
     *  same position. */
    public void addTile(Tile tile) {
        board.addTile(tile);
        checkGameOver();
        setChanged();
    }

    /**
     * 将棋盘向某个方向倾斜。
     * 核心逻辑：
     * 1. 保存当前棋盘的快照 (savedState)，用于判断合并条件（只和原始值合并）。
     * 2. 对每一列，从上往下 (Row 1 -> Row 3) 遍历。
     * 3. 只要上方有空位，就一直往上移 (Move to 0)。
     * 4. 如果上方有障碍，检查 savedState 是否允许合并。
     */
    public boolean tilt(Side side) {
        boolean changed;
        changed = false;

        // TODO: Modify this.board (and perhaps this.score) to account
        // for the tilt to the Side SIDE. If the board changed, set the
        // changed local variable to true.
        board.setViewingPerspective(side);
        for(int col=0;col<board.size();col++){
            if(deal_one_column(col)){
                changed = true;
            }
        }
        board.setViewingPerspective(Side.NORTH);
        checkGameOver();
        if (changed) {
            setChanged();
        }
        return changed;
    }
    public int find_before_row(int col,int row){
        for (row=row+1;row<board.size();row++){
            if(board.tile(col,row)!=null)return row;
        }
        return board.size();
    }
    public boolean deal_one_column(int col){
        boolean changed = false;
        boolean merged=false;
        for(int row = board.size()-1; row >= 0; row--){
            if(row == board.size()-1) continue;
            Tile tile_now = board.tile(col, row);
            if (tile_now == null) continue;
            int fail_find=board.size();
            int find_out=find_before_row(col,row);
            if(find_out!=fail_find){
                if(tile_now.value()==board.tile(col,find_out).value() && !merged){
                    board.move(col,find_out,tile_now);
                    score+=2*tile_now.value();
                    merged=true;
                    changed=true;
                }
                else {
                    if(row!=find_out-1){
                        board.move(col,find_out-1,tile_now);
                        merged=false;
                        changed=true;
                    }
                }
            }
            else {
                board.move(col,board.size()-1,tile_now);
                changed=true;
            }
        }
        return changed;
    }

    /** Checks if the game is over and sets the gameOver variable
     *  appropriately.
     */
    private void checkGameOver() {
        gameOver = checkGameOver(board);
    }

    /** Determine whether game is over. */
    private static boolean checkGameOver(Board b) {
        return maxTileExists(b) || !atLeastOneMoveExists(b);
    }

    /** Returns true if at least one space on the Board is empty.
     *  Empty spaces are stored as null.
     * */
    public static boolean emptySpaceExists(Board b) {
        // TODO: Fill in this function.
        for (int i=0;i<b.size();i++){
            for (int j=0;j<b.size();j++){
                if (b.tile(i,j)==null){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if any tile is equal to the maximum valid value.
     * Maximum valid value is given by MAX_PIECE. Note that
     * given a Tile object t, we get its value with t.value().
     */
    public static boolean maxTileExists(Board b) {
        // TODO: Fill in this function.
        for (int i=0;i<b.size();i++){
            for (int j=0;j<b.size();j++){
                if (b.tile(i,j)!=null) {
                    if (b.tile(i, j).value() == MAX_PIECE) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Returns true if there are any valid moves on the board.
     * There are two ways that there can be valid moves:
     * 1. There is at least one empty space on the board.
     * 2. There are two adjacent tiles with the same value.
     */
    public static boolean atLeastOneMoveExists(Board b) {
        // TODO: Fill in this function.
        for (int i=0;i<b.size();i++){
            for (int j=0;j<b.size();j++){
                if (b.tile(i,j)==null){
                    return true;
                }
                else {
                    int me = b.tile(i,j).value();
                    if((j-1>=0 && b.tile(i,j-1)!=null && b.tile(i,j-1).value()==me)||
                      (j+1<b.size() && b.tile(i,j+1)!=null && b.tile(i,j+1).value()==me)||
                      (i-1>=0 && b.tile(i-1,j)!=null && b.tile(i-1,j).value()==me)||
                      (i+1<b.size() && b.tile(i+1,j)!=null && b.tile(i+1,j).value()==me))
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    @Override
     /** Returns the model as a string, used for debugging. */
    public String toString() {
        Formatter out = new Formatter();
        out.format("%n[%n");
        for (int row = size() - 1; row >= 0; row -= 1) {
            for (int col = 0; col < size(); col += 1) {
                if (tile(col, row) == null) {
                    out.format("|    ");
                } else {
                    out.format("|%4d", tile(col, row).value());
                }
            }
            out.format("|%n");
        }
        String over = gameOver() ? "over" : "not over";
        out.format("] %d (max: %d) (game is %s) %n", score(), maxScore(), over);
        return out.toString();
    }

    @Override
    /** Returns whether two models are equal. */
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (getClass() != o.getClass()) {
            return false;
        } else {
            return toString().equals(o.toString());
        }
    }

    @Override
    /** Returns hash code of Model’s string. */
    public int hashCode() {
        return toString().hashCode();
    }
}
