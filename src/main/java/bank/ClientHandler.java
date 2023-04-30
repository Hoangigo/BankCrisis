package bank;
import borse.Code;
import borse.Message;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;

public class ClientHandler extends Thread {
  public static final String REQUEST_CANNOT_BE_PROCESSED = "500 Request cannot be processed.";
  public static final String BAD_REQUEST = "400 Request is missing Content-Length or Content-Type is not text/plain";
  public static final String DEFAULT_ANSWER = "200 OK";
  public static final int CONTENT_LENGTH_BEGIN_INDEX = 16;
  private static final int CONTENT_TYPE_BEGIN_INDEX = 14;
  private static final int MONEY_BEGIN_INDEX = 7;
  private static final int CODE_BEGIN_INDEX = 6;
  private static final int QUANTITY_BEGIN_INDEX = 10;
  private final Socket connection;
  private final BufferedReader reader;
  private final OutputStream writer;
  private int contentLength;
  private String contentType;
  private String statusMessage;
  private HashMap<Code, Message> savedMessage;

  private int moneyBeingRequest;
  private Code codeBeingRequest;
  private int amountOfCodeBeingRequest;
  private int currentValue;
  private final BufferedReader in;
  private final OutputStream out;
  private boolean checkRequest ;
  public ClientHandler(Socket clientConnection,HashMap<Code, Message> savedMessage, int currentValue)
      throws IOException {
    //TODO why ???
    System.out.println("Connection port should be 8080 "+clientConnection.getPort());
    this.savedMessage= savedMessage;
    connection = clientConnection;
    reader = new BufferedReader(new InputStreamReader(clientConnection.getInputStream()));
    writer = clientConnection.getOutputStream();
    this.currentValue = currentValue;
    in = new BufferedReader(new InputStreamReader(clientConnection.getInputStream()));
    out = clientConnection.getOutputStream();
    this.checkRequest = false;
  }
  @Override
  public synchronized void run() {
    try {
      String bodyContent;
      while (!connection.isClosed()) {
        System.out.println("Parse Header");
        parseHeader();

        bodyContent = getBody();
        System.out.println("get Body: "+ bodyContent);
        System.out.println(bodyContent);
        if (bodyContent == null)
          break;
        //sharedContainer.add(bodyContent);
        sendAnswer();
      }
    } catch (IOException ignored) {}
  }
  private void parseHeader() throws IOException {
    statusMessage = DEFAULT_ANSWER;
    contentLength = -1;
    contentType = "NOT OKAY";
    String data;
    if (!requestLineIsValid(reader.readLine())) {
      contentLength = 0;
      statusMessage = REQUEST_CANNOT_BE_PROCESSED;
      System.out.println("FALSE");
      sendAnswer();
      return;
    }
    if(this.checkRequest) {
      //sendAnswer();
      System.out.println("check Rquest");
      setCheckRequest(false);
      return;
    }
    data = reader.readLine();
    while (data != null && !data.equals("")) {
      if (data.startsWith("Content-Length: "))
        contentLength = getContentLength(data);
      if (data.startsWith("Content-Type: "))
        contentType = getContentType(data);
      if (data.startsWith("money: ")) {
        moneyBeingRequest = getMoney(data);
      }
      if (data.startsWith("code: ")) {
        codeBeingRequest = getCode(data);

      }
      if(data.startsWith("quantity: ")) {
        amountOfCodeBeingRequest = getQuantity(data);
        System.out.println("Amount: "+amountOfCodeBeingRequest);

      }
      data = reader.readLine();
      System.out.println(data);
    }
    if (requestMissesProperHeaderFields()) {
      contentLength = 0;
      statusMessage = BAD_REQUEST;
      sendAnswer();
    }
  }
  private int getMoney(String data){
    System.out.println(data);
    try {
      return Integer.parseInt(data.substring(MONEY_BEGIN_INDEX));
    } catch (Exception ignored) {}
    return 0;
  }
  private Code getCode(String data){
    System.out.println(data);
    try {
      return Code.valueOf(data.substring(CODE_BEGIN_INDEX));
    } catch (Exception ignored) {}
    return null;
  }
  private int getQuantity(String data){
    System.out.println(data);
    try {
      return Integer.parseInt(data.substring(QUANTITY_BEGIN_INDEX));
    } catch (Exception ignored) {}
    return 0;
  }
  private String getBody() throws IOException {
    if (contentLength == 0)
      return null;
    StringBuilder data = new StringBuilder();
    while(contentLength > 0) {
      int i = reader.read();
      data.append((char)i);
      --contentLength;
    }
    return data.toString();
  }
  private void sendAnswer() throws IOException {
    String http = "HTTP/1.1 " + statusMessage + "\r\n"
        + "Bank: Commerzbank\r\n"
        + "Content-Type: text/plain\r\n"
        + "Content-Length: 0\r\n\r\n";
    writer.write(http.getBytes());
  }
  private String getContentType(String data) {
    try {
      return data.substring(CONTENT_TYPE_BEGIN_INDEX);
    } catch (Exception ignored) {}
    return "NOT OKAY";
  }
  private int getContentLength(String data) {
    try {
      return Integer.parseInt(data.substring(CONTENT_LENGTH_BEGIN_INDEX));
    } catch (Exception ignored) {}
    return -1;
  }
  private boolean requestLineIsValid(String data) throws IOException {
    if (data == null)
      return false;
    String[] tokens = data.split(" ");
    if (tokens.length != 3)
      return false;
    if(tokens[0].equals("GET")&& tokens[1].equals("/getData") && tokens[2].equals("HTTP/1.1")){
      System.out.println("Get Request received");
      statusMessage = DEFAULT_ANSWER;
      String http = "HTTP/1.1 " + statusMessage + "\r\n"
          + "Server: bank9000\r\n"
          + "Content-Type: text/plain\r\n"
          + "Content-Length: "+ String.valueOf(this.currentValue+2).length()+ " \r\n\r\n"
          + currentValue+ " $";
      out.write(http.getBytes());
      out.flush();
      String leftOver;

      while (!(leftOver = in.readLine()).equals("")) {
        System.out.println(leftOver);
      }
      this.checkRequest= true;

      return true;
    }
    return tokens[0].equals("POST") && tokens[1].equals("/") && tokens[2].equals("HTTP/1.1");
  }
  private boolean requestMissesProperHeaderFields() {
    return contentLength == -1 || !contentType.equals("text/plain");
  }
  public boolean isCheckRequest() {
    return checkRequest;
  }

  public void setCheckRequest(boolean checkRequest) {
    this.checkRequest = checkRequest;
  }


}
