import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.util.List;

/**
 * Created by Fernando Rojas y Ana Laura Vargas on 27/3/2018.
 */

public class Server {

    private int port;

    //Constructor
    Server (int port){
        this.port = port;
    }

    /**
     * Listens for requests to the socket and creates a thread that attends each one
     * @throws IOException
     */
    public void listen() throws IOException {

        ServerSocket serverListener = new ServerSocket(port);
        System.out.println("Running server at: " + port);

        Socket requester;
        while(true) {
            requester = serverListener.accept(); //the server accepts the new client
            Thread thread = new Thread(new RequestHandler(requester)); //each request is a new thread
            thread.start();
        }
    }
}
