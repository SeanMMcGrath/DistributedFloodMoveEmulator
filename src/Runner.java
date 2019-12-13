import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;

/**
 * FloodMoveEmulator
 *
 * @author - Sean McGrath
 */
public class Runner extends RecursiveTask<Puzzle> {


    final Puzzle board;
    final int depth;
    final int MAX_DEPTH;
    final String[] ips = {"129.3.20.61", "129.3.20.62", "129.3.20.63", "129.3.20.64", "129.3.20.65", "129.3.20.66"};


    public Runner(Puzzle board , int depth, int MAX_DEPTH){
        this.board = board;
        this.depth = depth;
        this.MAX_DEPTH = MAX_DEPTH;
    }

    @Override
    protected Puzzle compute() {

        if(DistributedMain.remote) {
            //if remote run as normal
            if (board.isDone() || depth == MAX_DEPTH) {
                //if board is fully flooded or depth limit reached, then return self
                return board;
            }
            final ArrayList<Color> moves = board.possibleMoves();
            //moves to do so make and invoke them
            final ArrayList<Runner> toInvoke = new ArrayList<>();
            for (Color c : moves) {
                Puzzle p = board.clone();
                p.depth++;
                p.memory = board;
                p.flood(c);
                Runner r = new Runner(p, depth + 1, MAX_DEPTH);
                toInvoke.add(r);
            }

            //invoke
            List<Runner> results = (List<Runner>) invokeAll(toInvoke);
            //find and return best result
            return getBest(results).board;
        } else {
            //if not remote, then split up work and instead of invoking send the new Runner's to the remote servers to run
            final ArrayList<Color> moves = board.possibleMoves();
            //find all moves and send each to server
            final ArrayList<Runner> toSend = new ArrayList<>();
            for (Color c : moves) {
                Puzzle p = board.clone();
                p.depth++;
                p.memory = board;
                p.flood(c);
                Runner r = new Runner(p, depth + 1, MAX_DEPTH);
                toSend.add(r);
            }
            int port = 13188;

            try {

                for(int i = 0; i < toSend.size(); i++){
                    Socket socket = new Socket(ips[i], port);
                    System.out.println("Attempting to send object to remote server");
                    ObjectOutputStream o = new ObjectOutputStream(socket.getOutputStream());
                    o.writeObject(toSend.get(i));
                    o.close();
                    socket.close();
                }


                List<Runner> results = new ArrayList<>();
                for (int i = 0; i < toSend.size(); i++) {
                    Socket socket = new Socket(ips[i], port);
                    ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                    Puzzle p = (Puzzle) in.readObject();
                    Runner r = new Runner(p, p.depth, MAX_DEPTH);
                    results.add(r);
                }
                return getBest(results).board;//result;


            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        }
    }


    /**
     * takes a list of results and finds the one with the largest flood size, prioritizing smaller depths
     * @param results - list of results
     * @return - best result
     */
    private Runner getBest(List<Runner> results){
        Runner best = null;
        int shallowest = -1;

        for (Runner r : results) {//get the runner with the largest flood size
            if (best == null) {
                best = r;
                shallowest = r.depth;
            } else {
                if(shallowest > r.depth) {
                    //smaller depth so set as best
                    best = r;
                    shallowest = r.depth;
                } else if(shallowest == r.depth) {
                    if (best.board.getMainPoints().size() < r.board.getMainPoints().size()) {//same depth so check if better size
                        best = r;
                    }
                }
                //if shallowest < depth then ignore it since its an inferior result
            }
        }
        return best;
    }
}
