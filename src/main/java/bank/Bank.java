package bank;

import borse.Code;
import borse.Message;
import connection.Establisher;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import udphandler.UDPHandler;
import udphandler.UDPMessage;

/**
 * Sensor which generates random data
 * and communicates using a DatagramSocket.
 */
public class Bank extends Thread   {
    private final UDPHandler handler;
    private final String bankName;
    private int currentValue;
    private int HTTP_DEFAULT_PORT;
    private int port;

    private ClientHandler clientHandler;
    private final ServerSocket serverSocket;

    private HashMap<Code, Message> savedMessage;
    private boolean running;


    public Bank(String name, int port, int httpPort) throws IOException {
        this.HTTP_DEFAULT_PORT= httpPort;
        this.bankName = name;
        this.currentValue = 0;
        savedMessage = new HashMap<>();
        this.port = port;
        this.running = true;
        serverSocket = new ServerSocket(HTTP_DEFAULT_PORT);
        this.handler = new UDPHandler(this) {
            @Override
            public UDPMessage getMessage() {
                String message = bankName+": RTT check" ;
                return new UDPMessage(message);
            }
        };
    }
    @Override
    public void run() {
        handler.start();
        try {
            while (running) {
                Socket client = serverSocket.accept();
                this.clientHandler = new ClientHandler(client, savedMessage, currentValue);
                this.clientHandler.start();
            }
        } catch (Exception ignored) {
        }
    }
    public HashMap<Code, Message> getSavedMessage() {
        return savedMessage;
    }

    public int getPort(){
       return this.port;
    }

    public void addSavedMessage(Code code, int quantity, int price){
        Message msg = savedMessage.get(code);
        if (msg == null) {
            msg = new Message(code, quantity, price);
            savedMessage.put(code, msg);
        } else {
            int newQuantity = msg.getQuantity() + quantity;
            int newPrice = price;
            msg.setQuantity(newQuantity);
            msg.setPrice(newPrice);
            savedMessage.replace(code, msg);
        }
    }
    /*
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
    }*/

    public int getCurrentValue() {
        return currentValue;
    }
    public void setCurrentValue(int value) {
        this.currentValue = value;
    }



}
