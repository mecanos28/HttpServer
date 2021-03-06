import java.io.*;
import java.net.Socket;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Fernando Rojas y Ana Laura Vargas on 27/3/2018.
 */
public class RequestHandler implements Runnable{

    private Socket requester;
    private ArrayList<String> requestHeaders;
    private ArrayList<String> responseHeaders;
    private DateFormat dateFormat;
    private Date date;
    private Method method;
    private String version;
    private String id;
    private byte[] body;
    private String postData;
    private String [] log;
    private String referer;
    private String acceptType;
    private String idType;

    //Constructor
    public RequestHandler(Socket client) {
        this.requester = client;
        requestHeaders = new ArrayList<>();
        responseHeaders = new ArrayList<>();
        dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        date = new Date();
        //System.out.println(dateFormat.format(date));
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        log = new String[6]; //0 Método, 1 Timestamp, 2 Servidor, 3 Refiere (De donde viene), 4 URL, 5 Datos (POST)
        log[1]=timestamp.toString();
        log[2]="ServidorDeAnaYFernando";
        acceptType = ".";
        idType = "..";
    }

    public enum ContentType {
        GIF("GIF"),
        HTML("HTML"),
        JPG("JPG"),
        CSS("CSS"),
        JPEG("JPEG"),
        PNG("PNG"),
        TXT("TXT"),
        PLAIN("PLAIN");

        private String type;

        ContentType(String mimeType) {
            this.type = mimeType;
        }

        public String getString() {
            switch (this) {
                case CSS:
                    return "Content-Type: text/css";
                case GIF:
                    return "Content-Type: image/gif";
                case HTML:
                    return "Content-Type: text/html";
                case JPG:
                case JPEG:
                    return "Content-Type: image/jpeg";
                case PNG:
                    return "Content-Type: image/png";
                case PLAIN:
                case TXT:
                    return "Content-type: text/plain";
                default:
                    return null;
            }
        }
    }

    public enum Method {
        GET("GET"),
        HEAD("HEAD"),
        POST("POST"),
        UNKNOWN("UNKNOWN");

        private String method;

        Method(String method) {
            this.method = method;
        }
    }

    public enum Status {
        _200("200 OK"),
        _404("404 Not Found"),
        _406("406 Not Acceptable"),
        _501("501 Not Implemented");

        private String status;

        Status(String status) {
            this.status = status;
        }

        public String getString()
        {
            return status;
        }
    }

    @Override
    public void run() {
        try {
            readRequest();
            createResponse();
            printLog();
            requester.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads the html request and initializes global variables that may be neaded
     * @throws IOException
     */
    private void readRequest() throws IOException {
        DataInputStream reader = new DataInputStream(new DataInputStream(requester.getInputStream()));
        String iter = reader.readLine();
        requestMethodReader(iter); //Read the method
        int contentLength = 0; //POST CONTENT LENGTH

        while (!iter.equals("")) { //While it can read the header
            iter = reader.readLine();
            requestHeaders.add(iter);

            if(iter.toUpperCase().startsWith("REFERER:")) //Fill referer
            {
                try {
                    referer = (iter.substring(8).trim());
                    log[3] = referer;
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }

            if(iter.toUpperCase().startsWith("ACCEPT:")) //Fill accept
            {
                try {
                    acceptType = (iter.substring(7).trim());
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }

            if(method.toString().equals("POST")){ //If it is a post, get Content length
                if(iter.toUpperCase().startsWith("CONTENT-LENGTH:"))
                {
                    try {
                        contentLength = Integer.parseInt(iter.substring(15).trim());
                    }
                    catch(Exception e) {
                        e.printStackTrace();
                        contentLength=0;
                    }
                }
            }
        }
        //Read the HTTP POST content
        if(contentLength > 0) //If it was a post, with the content length it can keep reading to get the variables
        {
            int readed = 0;
            int readThisIteration;
            byte[] buffer =new byte[1024];

            while((readThisIteration=reader.read(buffer,0,1024))!= -1)
            {
                readed += readThisIteration;
                if(readed >= contentLength) {
                    postData = new String(buffer, 0, readThisIteration);
                    log[5] = postData;
                    break;
                }
            }
        }
    }

    /**
     * Reads the first line of the request and gets the method, id of the resource and http version
     * @param firstLine
     */
    private void requestMethodReader(String firstLine) {
        String[] line = firstLine.split("\\s+"); //Split at the whitespace using Regex
        try {
            method = Method.valueOf(line[0]);
            log[0] = method.toString(); //Method to log
        } catch (Exception e) {
            log[0] = Method.UNKNOWN.toString();
            method = Method.UNKNOWN;
        }
        id = line[1];
        version = line[2];
    }

    /**
     * Creates, constructs and sends the response depending on the method
     * @throws IOException
     */
    private void createResponse() throws IOException {
        switch (method) {
            case HEAD:
                writeResponseHeaders(Status._200); //Fills headers if it's just a head
                break;
            case POST: //POST and GET do the same thing in this program, POST variables are not treated
            case GET:
                try {
                    log[4] = id; //URL to log
                    File file = new File("." + id);
                    if(!file.exists()){ //If the file doesn't exist, 404 Error not found
                        writeResponseHeaders(Status._404);
                        setResponseBody(Status._404.getString());
                        //System.out.println("Error 404 No Encontrado");
                    }
                    else { //If the file does exist, check if 406, otherwise write the response
                        checkTypes(id);
                        setResponseBody(getFileByteArray(file));
                        if(!(acceptType.equals("*")) && !(acceptType.equals("/")) && !(checkTypes())){ //Accept type must be the same as resource type
                            writeResponseHeaders(Status._406);
                            setResponseBody(Status._406.getString());
                            //System.out.println("Error 406 No Aceptable");
                        }
                        else writeResponseHeaders(Status._200);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            default: //If method is not implemented (Not GET, POST or HEAD)
                writeResponseHeaders(Status._501);
                setResponseBody(Status._501.getString());
                //System.out.println("Error 501 No implementado");
        }
        writeResponse();
    }

    /**
     * Checks if id and accept type are the same
     * @return true or false
     */
    private boolean checkTypes(){
        String contentOfType = ContentType.valueOf(idType.toUpperCase()).getString();
        String contentOfAccept = ContentType.valueOf(acceptType.toUpperCase()).getString();
        return contentOfType.equals(contentOfAccept);
    }

    /**
     * Writes in the socket output stream the header and the corresponding body if needed
     * @throws IOException
     */
    private void writeResponse() throws IOException {
        DataOutputStream output = new DataOutputStream(requester.getOutputStream());
        for (String header : responseHeaders) {
            output.writeBytes(header + "\r\n");
            System.out.println(header);
        }
        output.writeBytes("\r\n");
        if (body != null) {
            output.write(body);
        }
        output.writeBytes("\r\n");
        output.flush();
    }

    /**
     * Sets the content types of the accept and the resource in the global variables
     * @param id id of the resource to be checked
     */
    private void checkTypes(String id) {
        try {
            acceptType = acceptType.substring(acceptType.indexOf("/") + 1);
            idType = id.substring(id.indexOf(".") + 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *Writes the response headers in the header array
     * @param status The status of the request, if 200 fills Date and Referer
     */
    private void writeResponseHeaders(Status status) {
        responseHeaders.add("HTTP/1.0 " + status.getString());
        if(method.toString().equals("GET") || method.toString().equals("POST")) //If it is a POST OR GET, THEN FILL CONTENT TYPE IN HEADER
            if(!idType.equals("..")) {
                responseHeaders.add(ContentType.valueOf(idType.toUpperCase()).getString());
            }
        responseHeaders.add("Connection: close");
        responseHeaders.add("Server: ServidorDeAnaYFernando");
        if(status.getString().equals("200 OK")){
            responseHeaders.add("Date: "+ dateFormat.format(date));
            responseHeaders.add("Referer: "+ referer);
        }
    }

    /**
     * Fill the response for errors
     * @param response
     */
    private void setResponseBody(String response) {
        body = response.getBytes();
    }

    /**
     * Fill the response for actual bodies
     * @param response The file to be written
     */
    private void setResponseBody(byte[] response) {
        body = response;
    }

    /**
     *
     * @param file The file to be converted to byteArray
     * @return returns the file´s byte array
     * @throws IOException
     */
    private byte[] getFileByteArray(File file) throws IOException {
        int length = (int) file.length();
        byte[] array = new byte[length];
        InputStream in = new FileInputStream(file);
        int os = 0; //offset for next inputstream read
        while (os < length) {
            int count = in.read(array, os, (length - os));
            os += count;
        }
        in.close();
        return array;
    }

    /**
     * Prints log information
     */
    private void printLog() {
        System.out.println("\n-------------------------------------------------------------------------------------------------------------------------------");
        System.out.println("Método: " + log[0] + "\nTimestamp: " + log[1] + "\nServidor: " + log[2] + "\nRefiere: " + log[3] + "\nURL: " + log[4] + "\nDatos: " + log[5]);
        System.out.println("-------------------------------------------------------------------------------------------------------------------------------\n");
    }

}
