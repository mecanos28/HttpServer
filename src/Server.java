import com.sun.xml.internal.bind.v2.TODO;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by feroj_000 on 27/3/2018.
 */
public class Server {
    private ServerSocket serverListener;
    public int port;

    Server (int port){this.port=port;} //Constructor
    public void listen() throws IOException {
        serverListener = new ServerSocket(port);
        Socket requester = null;

        while(true){
            requester = serverListener.accept(); //se acepta el nuevo cliente al servidor
            Thread thread = new Thread(new ...); //TODO para cada thread crear un nuevo objeto de la clase que maneja requestst http
            thread.start();
    }



    }
}
