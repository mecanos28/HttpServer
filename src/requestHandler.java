import java.io.*;
import java.net.Socket;

/**
 * Created by feroj_000 on 27/3/2018.
 */
public class requestHandler implements Runnable{
    protected Socket requester;
    public requestHandler(Socket client) {this.requester = client;} //Constructor

    @Override
    public void run() {
        try {
            readRequest();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void readRequest() throws IOException, InterruptedException{
        try{
            BufferedWriter response = new BufferedWriter(
                    new OutputStreamWriter(this.requester.getOutputStream()));
            BufferedReader request = new BufferedReader(new InputStreamReader(
                    this.requester.getInputStream()));

            String reqHeader = "";
            String iterator = "";
            while (!iterator.equals("")) { //Lee el request y lo guarda en requestHeader
                iterator = request.readLine();
                reqHeader += iterator + "\n";
                //TODO: Guardar la información del header como el timestamp, la url y todo eso para la bitácora
            }

            //TODO: Fijarse en el tipo de request, y hacer un caso para cada tipo HEAD, GET, POST y error
            //TODO: Casi que todo es lo mismo que esto https://github.com/iamprem/HTTPclient-server


        }
        catch (Exception e){

        }
    }





}
