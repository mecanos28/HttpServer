import java.io.*;
import java.net.Socket;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by feroj_000 on 27/3/2018.
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
    private int bodyLength;
    private byte[] body;
    private String postData;
    private String [] log;
    private String referer;

    //Constructor
    public RequestHandler(Socket client) throws IOException, InterruptedException {
        this.requester = client;
        requestHeaders = new ArrayList<>();
        responseHeaders = new ArrayList<>();

        dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        date = new Date();
        System.out.println(dateFormat.format(date));
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        log = new String[6]; //0 Método, 1 Timestamp, 2 Servidor, 3 Refiere (De donde viene), 4 URL, 5 Datos (POST)
        log[1]=timestamp.toString();
        log[2]="ServidorDeAnaYFernando";
    }

    public enum ContentType {
        GIF("GIF"),
        HTML("HTML"),
        JPG("JPG"),
        CSS("CSS"),
        JPEG("JPEG"),
        PNG("PNG"),
        TXT("TXT");

        private String type;

        //Constructor
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
        POST("POST");

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
            requester.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void readRequest() throws IOException, InterruptedException{
        DataInputStream reader = new DataInputStream(new DataInputStream(requester.getInputStream()));
        String iter = reader.readLine();
        System.out.println(iter);
        requestMethodReader(iter);
        int contentLength=0; //POST CONTENT LENGTH

        while (!iter.equals("")) {
            iter = reader.readLine();
            requestHeaders.add(iter);
            System.out.println(iter);

            if(iter.toUpperCase().startsWith("REFERER:"))
            {
                try {
                    referer=(iter.substring(8).trim());
                    System.out.println("Found the HTTP referer: "+referer);
                    log[3]=referer;
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }

            if(method.toString() == "POST"){
                if(iter.toUpperCase().startsWith("CONTENT-LENGTH:"))
                {
                    try {
                        contentLength=Integer.parseInt(iter.substring(15).trim());
                        System.out.println("Found the HTTP Content-Length header: "+contentLength);
                    }
                    catch(Exception e) {
                        e.printStackTrace();
                        contentLength=0;
                    }
                }
            }
        }

        //Read the HTTP POST content
        if(contentLength>0)
        {
            int readed=0;
            int c;
            byte[] buffer=new byte[1024];

            while((c=reader.read(buffer,0,1024))!=-1)
            {
                readed+=c;
                System.out.println(new String(buffer,0,c));
                if(readed>=contentLength) {
                    postData = new String(buffer, 0, c);
                    log[5]=postData;
                    break;
                }
            }
        }
    }


    private void requestMethodReader(String str) {
        String[] line = str.split("\\s+");
        try {
            method = Method.valueOf(line[0]);
        } catch (Exception e) {
        }
        id = line[1];
        version = line[2];
        log[0]=method.toString(); //Method to log
    }

    public void createResponse() throws IOException {
        switch (method) {
            case HEAD:
                fillHeaders(Status._200);
                break;
            case POST: //No es igual que el GET
            case GET:
                try {
                    fillHeaders(Status._200);
                    log[4]=id; //URL to log
                    File file = new File("." + id);
                    if (file.exists()) {
                        setContentType(id, responseHeaders);
                        fillResponse(getBytes(file));
                    } else {
                        fillHeaders(Status._404);
                        fillResponse(Status._404.getString());
                        System.out.println("Error 404 No implementado");
                    }
                } catch (Exception e) {
                }
                break;
            default:
                fillHeaders(Status._501);
                fillResponse(Status._501.getString());
                System.out.println("Error 501 No implementado");
        }
        writeResponse();
    }

    private void writeResponse() throws IOException {
        DataOutputStream output = new DataOutputStream(requester.getOutputStream());
        for (String header : responseHeaders) {
            output.writeBytes(header + "\r\n");
        }
        output.writeBytes("\r\n");
        if (body != null) {
            output.write(body);
        }
        output.writeBytes("\r\n");
        System.out.println(output);
        output.flush();
    }

    private void setContentType(String id, List<String> list) {
        try {
            String ext = id.substring(id.indexOf(".") + 1);
            list.add(ContentType.valueOf(ext.toUpperCase()).toString());
            System.out.println("CONTENT TYPE "+ ContentType.valueOf(ext.toUpperCase()).toString());
        } catch (Exception e) {
        }
    }

    private void fillHeaders(Status status) {
        responseHeaders.add("HTTP/1.0 " + status.getString());
        responseHeaders.add("Connection: close");
        responseHeaders.add("Server: ServidorDeAnaYFernando");
        if(status.getString().equals("200 OK")){
            responseHeaders.add("Content-length: "+ this.bodyLength);
            responseHeaders.add("Date: "+ dateFormat.format(date));
            //Host
            //Referer
            //Server
        }
    }

    private void fillResponse(String response) {
        body = response.getBytes();
    }

    private void fillResponse(byte[] response) {
        body = response;
    }

    private byte[] getBytes(File file) throws IOException {
        int length = (int) file.length();
        this.bodyLength=length;
        byte[] array = new byte[length];
        InputStream in = new FileInputStream(file);
        int offset = 0;
        while (offset < length) {
            int count = in.read(array, offset, (length - offset));
            offset += count;
        }
        in.close();
        return array;
    }

}
