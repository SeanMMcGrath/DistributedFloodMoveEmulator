import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.concurrent.ForkJoinPool;

/**
 * DistributedFloodMoveEmulator
 *
 * @author - Sean McGrath
 */
public class DistributedMain {

    private static final String ipAddress = "";//ip address of my pc when in computer lab
    private static final int port = 13188;
    private static Socket socket;

    public static void main(String[] args) {

        try {
            ServerSocket ss = new ServerSocket(port);
            while (true) {
                System.out.println("Waiting on object");
                System.out.println("Waiting on port#" + port);

                socket = ss.accept();
                System.out.println("Connected");
                ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());

                //receive object
                Runner in = (Runner) inStream.readObject();

                inStream.close();
                socket.close();

                ForkJoinPool pool = new ForkJoinPool(2);

                //invoke and get result
                Puzzle out = pool.invoke(in);
                System.out.println("Returning Result");
                //return result

                socket = ss.accept();
                ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
                outStream.writeObject(out);
                System.out.println("Object returned, waiting for new input.");
                outStream.close();
                socket.close();
            }
            //ss.close();
        } catch (IOException | ClassNotFoundException ioe) {
            ioe.printStackTrace();
        }
    }
}
