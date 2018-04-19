import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;

/**
 * Created by feroj_000 on 27/3/2018.
 */

//TODO: Crear otra clase que sea como una consola en donde se inicie el servidor que tenga una instancia de esta clase

public class Server {

    private ServerSocket serverListener;
    private int port;

    //Constructor
    Server (int port){
        this.port = port;
    }

    /**
     * Listens for requests to the socket and creates a thread that attends each one
     * @throws IOException
     * @throws InterruptedException
     */
    public void listen() throws IOException, InterruptedException {

        serverListener = new ServerSocket(port);
        System.out.println("Running server at: " + port);

        Socket requester = null;
        System.out.println("Waiting for clients");
        while(true){
            requester = serverListener.accept(); //acepta el nuevo cliente al servidor
            Thread thread = new Thread( new RequestHandler(requester) ); //cada request es un hilo nuevo
            thread.start();
        }
    }
}
