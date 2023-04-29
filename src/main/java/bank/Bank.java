package bank;

import borse.Code;
import borse.Message;
import connection.Establisher;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import udphandler.UDPHandler;
import udphandler.UDPMessage;

/**
 * Sensor which generates random data
 * and communicates using a DatagramSocket.
 */
public class Bank implements Establisher<Socket> {
    private final UDPHandler handler;
    /**
     * Name of this bank.
     */
    private final String bankName;
    private int currentValue;
    private final int HTTP_DEFAULT_PORT = 8080;
    private int port;

    public HashMap<Code, Message> getSavedMessage() {
        return savedMessage;
    }

    private HashMap<Code, Message> savedMessage;
    /**
     * TCP-Socket for communication between bank and browser.
     */
    //private final Socket connection;

    /**
     * Reads from the input stream of the Socket.
     */
    //private final BufferedReader reader;

    /**
     * Writes to the output stream of the Socket.
     */
    //private final OutputStream writer;

    public Bank(String name, int port,String serverName) throws IOException {
        //this.connection = establishConnection(serverName, HTTP_DEFAULT_PORT);
        //this.reader = new BufferedReader(new InputStreamReader(this.connection.getInputStream()));
        //this.writer = this.connection.getOutputStream();
        this.bankName = name;
        this.currentValue = 0;
        savedMessage = new HashMap<>();
        this.port = port;
        this.handler = new UDPHandler(this) {
            @Override
            public UDPMessage getMessage() {
                String message = bankName+": RTT check" ;
                return new UDPMessage(message);
            }
        };
    }
    public int getPort(){
       return this.port;
    }
    public void start() {
        handler.start();
    }
    public void addSavedMessage(Code code, int quantity, int price){
        Message msg = savedMessage.get(code);
        if (msg == null) {
            msg = new Message(code, quantity, price);
            savedMessage.put(code, msg);
        } else {
            int newQuantity = msg.getQuantity() + quantity;
            int newPrice = price; // or you could calculate a new average price here
            msg.setQuantity(newQuantity);
            msg.setPrice(newPrice);
        }
    }
    @Override
    public Socket establishConnection(String host, int port) {
        Socket tmp = null;
        while (tmp == null) {
            try {
                tmp = new Socket(InetAddress.getByName(host), HTTP_DEFAULT_PORT);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        return tmp;
    }
    /*
    /**
     * Sends a Post request to the server
     * that contains the sensor data.
     * @param message Sensor data.
     */
    private void sendPostRequest(String message) throws IOException {
        String postRequest = "POST / HTTP/1.1\r\n"
            + "Host: bank\r\n"
            + "Content-Type: text/plain\r\n"
            + "Content-Length: " + message.length()+ "\r\n"
            +"\r\n" + message;
        //writer.write(postRequest.getBytes());
    }
    public int getCurrentValue() {
        return currentValue;
    }
    public void setCurrentValue(int value) {
        this.currentValue = value;
    }


    /**
     * Prints the server response to standard output.
     */
    /*
    private void getServerResponse() throws IOException {
        String line;
        while (!(line = reader.readLine()).equals(""))
            System.out.println(line);
    }*/
}
