import java.io.IOException;

/**
 * Created by Fernando Rojas y Ana Laura Vargas on 27/3/2018.
 */

public class RunServer {

    /**
     * Creates a server instance and runs its listener
     * @throws InterruptedException
     */
    public static void main (String[] args) throws InterruptedException
    {
        int port = 8080;

        try {
            Server myServer = new Server(port);
            myServer.listen();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

}
