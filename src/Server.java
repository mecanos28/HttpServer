import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;

/**
 * Created by feroj_000 on 27/3/2018.
 */

//TODO: Crear otra clase que sea como una consola en donde se inicie el servidor que tenga una instancia de esta clase

public class Server {
    protected ServerSocket serverListener;
    protected int port;

    Server (int port){this.port=port;} //Constructor
    public void listen() throws IOException {
        serverListener = new ServerSocket(port);
        Socket requester = null;
        while(true){
            requester = serverListener.accept(); //se acepta el nuevo cliente al servidor
            Thread thread = new Thread( new requestHandler(requester) ); //cada request es un hilo nuevo
            thread.start();
        }
    }
}
