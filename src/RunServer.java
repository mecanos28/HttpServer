import java.io.IOException;

public class RunServer {

    public static int port = 8080;

    public RunServer(){
    }

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
