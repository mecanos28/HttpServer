import java.io.*;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by feroj_000 on 27/3/2018.
 */
public class RequestHandler implements Runnable{
    protected Socket requester;

    ArrayList<String> requestheaders = new ArrayList<String>();
    ArrayList<String> responseheaders = new ArrayList<String>();
    Method method;
    String version;
    String id;
    int bodylength;
    byte[] body;
    DateFormat dateFormat;
    Date date;



    public RequestHandler(Socket client) throws IOException, InterruptedException {
        dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        date = new Date();
        Date date = new Date();
        System.out.println(dateFormat.format(date));
        this.requester = client;
        readRequest();

    } //Constructor

    public enum ContentType {
        GIF("GIF"),
        HTML("HTML"),
        JPG("JPG"),
        CSS("CSS"),
        JPEG("JPEG"),
        PNG("PNG"),
        TXT("TXT");

        private String tipo;

        ContentType(String mimeType) {
            this.tipo = mimeType;
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

        private final String method;

        Method(String method) {
            this.method = method;
        }
    }

    public enum Status {
        _200("200 OK"),
        _404("404 Not Found"),
        _406("406 Not Acceptable"),
        _501("501 Not Implemented");

        private final String status;

        Status(String status) {
            this.status = status;
        }

        public String getString() {
            return status;
        }
    }


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
        BufferedReader reader = new BufferedReader(new InputStreamReader(requester.getInputStream()));
        String iter = reader.readLine();
        requestMethodReader(iter);

        while (!iter.equals("")) {
            iter = reader.readLine();
            processHeader(iter);
        }
    }

    private void requestMethodReader(String str) {
        String[] line = str.split("\\s");
        try {
            method = Method.valueOf(line[0]);
        } catch (Exception e) {
        }
        id = line[1];
        version = line[2];
    }

    private void processHeader(String line) {
        requestheaders.add(line);
    }

    public void createResponse() throws IOException {
        switch (method) {
            case HEAD:
                fillHeaders(Status._200);
                break;
            case POST:
            case GET:
                try {
                    fillHeaders(Status._200);
                    File file = new File("." + id);
                    if (file.exists()) {
                        setContentType(id, responseheaders);
                        fillResponse(getBytes(file));
                    } else {
                        fillHeaders(Status._404);
                        fillResponse(Status._404.toString());
                    }
                } catch (Exception e) {
                }

                break;
            default:
                fillHeaders(Status._501);
                fillResponse(Status._501.toString());
        }
    }

    private void setContentType(String uri, List<String> list) {
        try {
            String ext = uri.substring(uri.indexOf(".") + 1);
            list.add(ContentType.valueOf(ext.toUpperCase()).toString());
        } catch (Exception e) {
        }
    }

    private void fillHeaders(Status status) { //HACER ESTE FILL HEADERS CON TODO
        if(status.getString().equals("200 OK")){
            responseheaders.add("HTTP/1.0 " + status.toString());
            responseheaders.add("Connection: close");
            responseheaders.add("Server: ServidorDeAnaYFernando");
            responseheaders.add("Content-length: "+ this.bodylength);
            responseheaders.add("Date: "+ dateFormat.format(date));
            //Host
            //Referer
            //Server
        }else{
            responseheaders.add("HTTP/1.0 " + status.toString());
            responseheaders.add("Connection: close");
            responseheaders.add("Server: SimpleWebServer");
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
        this.bodylength=length;
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
