import java.io.Serializable;
import java.util.ArrayList;

/**
 * FloodMoveEmulator
 *
 * @author - Sean McGrath
 */
enum Color {
    RED, BLUE, PURPLE, YELLOW, ORANGE, GREEN;
}

public class Puzzle implements Serializable {
    //n*n game board
    final Color[][] board;
    //last puzzle board
    Puzzle memory;
    //how deep this puzzle is
    public int depth;

    /**
     * first created
     */
    public Puzzle(Color[][] board){
        this.board = board;
        depth = 1;
        memory = null;//first so no prev board
    }

    /**
     * used for cloning
     */
    public Puzzle(Color[][] board, int depth){
        this.board = board;
        this.depth = depth;
    }

    /**
     * returns true if board is all one color
     * @return - true if done else false
     */
    public boolean isDone(){
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                if(!board[i][j].equals(board[0][0])){
                    return false;
                }
            }
        }
        return true;
    }

    public Puzzle clone(){
        Color[][] newArray = new Color[board.length][board.length];
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                newArray[i][j] = board[i][j];
            }
        }
        return new Puzzle(newArray, depth);
    }

    /**
     * @return - list of colors bordering current main color type
     */
    public ArrayList<Color> possibleMoves(){
        ArrayList<Color> result = new ArrayList<>();
        //get main color points
        ArrayList<Point> mainPoints = getMainPoints();

        //check all spots next to the main points and get all unique colors
        for (Point p: mainPoints) {
            if(p.x+1 < board.length){
                if(!result.contains(board[p.x+1][p.y]) && board[p.x+1][p.y] != board[0][0]){
                    result.add(board[p.x+1][p.y]);
                }
            }
            if(p.y+1 < board.length){
                if(!result.contains(board[p.x][p.y+1]) && board[p.x][p.y+1] != board[0][0]){
                    result.add(board[p.x][p.y+1]);
                }
            }
            if(p.x-1 > 0){
                if(!result.contains(board[p.x-1][p.y]) && board[p.x-1][p.y] != board[0][0]){
                    result.add(board[p.x-1][p.y]);
                }
            }
            if(p.y-1 > 0){
                if(!result.contains(board[p.x][p.y-1]) && board[p.x][p.y-1] != board[0][0]){
                    result.add(board[p.x][p.y-1]);
                }
            }
        }
        //if(result.size() == 0){return null;}
        return result;
    }

    /**
     * floods main color with Color c
     */
    public void flood(Color c){
        ArrayList<Point> toFlood = getMainPoints();
        for (Point p : toFlood) {
            board[p.x][p.y] = c;
        }
    }

    /**
     * gets an arraylist of points that are all the same color as top left corner and connected to it
     * @return - list of connected points
     */
    public ArrayList<Point> getMainPoints(){
        ArrayList<Point> result = new ArrayList<>();

        boolean changeMade;
        //loop until nothing new found
        //array so small that looping like this wont cost much
        do {
            changeMade = false;
            for (int i = 0; i < board.length; i++) {
                for (int j = 0; j < board.length; j++) {
                    if (board[i][j] == board[0][0]) {
                        Point tmp = new Point(i, j);
                        if (i == 0 && j == 0 && !result.contains(tmp)) {//if adjacent to node that has been visited already or first node
                            result.add(tmp);
                            changeMade = true;
                        } else {
                            if(!result.contains(tmp)) {
                                if(adjacentToKnown(i, j, result)) {
                                    result.add(tmp);
                                    changeMade = true;
                                }
                            }
                        }
                    }
                }
            }
        }while (changeMade);
        return result;
    }

    /**
     * finds if point x, y is next to a known point
     * @param x - x coord
     * @param y - y coord
     * @param known - known points
     * @return - true if adjacent, false otherwise
     */
    private boolean adjacentToKnown(int x, int y, ArrayList<Point> known){
        for (Point p : known) {
            if(p.x == x+1 && p.y == y  ||  p.x == x-1 && p.y == y  ||  p.x == x && p.y == y+1  ||  p.x == x && p.y == y-1){
                return true;
            }
        }
        return false;
    }
}

class Point implements Serializable{
    int x;
    int y;
    public Point(int x, int y){
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object object)
    {
        boolean sameSame = false;

        if (object != null && object instanceof Point)
        {
            sameSame = ((this.x == ((Point) object).x) && (this.y == ((Point) object).y));
        }

        return sameSame;
    }
}
