import java.io.IOException;

public class RunServer {

    public static int port = 8080;

    public RunServer(){
    }

    /**
     * Creates a server instance and runs its listener
     * @param args
     * @throws InterruptedException
     */
    public static void main (String[] args) throws InterruptedException
    {
        try {
            Server myServer = new Server(port);
            myServer.listen();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

}
