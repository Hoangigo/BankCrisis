package bank;

import borse.Code;
import borse.Message;
import connection.Establisher;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import thrift.BankService;
import thrift.BankService.Client;
import thrift.LoanRequest;
import thrift.LoanResponse;
import udphandler.UDPHandler;
import udphandler.UDPMessage;

/**
 * Sensor which generates random data
 * and communicates using a DatagramSocket.
 */
public class Bank extends Thread implements Establisher {

    private final UDPHandler handler;
    private final String bankName;
    private int currentValue;
    private int HTTP_DEFAULT_PORT;
    private int port;

    private ClientHandler clientHandler;
    private final ServerSocket serverSocket;
    private HashMap<Code, Message> savedMessage;
    private boolean running;
    private HashMap<String, Integer> rpcAdress;
    private boolean bankrupt;
    private RPCHelper rpc;




    public Bank(String name, int port, int httpPort) throws IOException {
        this.HTTP_DEFAULT_PORT = httpPort;
        this.bankName = name;
        this.currentValue = 0;
        savedMessage = new HashMap<>();
        this.port = port;
        this.running = true;
        serverSocket = new ServerSocket(HTTP_DEFAULT_PORT);
        this.bankrupt = false;
        rpc = new RPCHelper(this);
        String bankRPCs = System.getenv("RPCBANKS");
        if (bankRPCs != null) {
            rpcAdress= new HashMap<>();
            String[] bankEndpoints = bankRPCs.split(",");
            for (String endpoint : bankEndpoints) {
                String[] parts = endpoint.split(":");
                String bankName = parts[0];
                int portName = Integer.parseInt(parts[1]);
                rpcAdress.put(bankName,portName);
            }
        }
        this.handler = new UDPHandler(this) {
            @Override
            public UDPMessage getMessage() {
                String message = bankName + ": RTT check";
                return new UDPMessage(message);
            }
        };
    }


    public boolean isBankrupt() {
        return bankrupt;
    }

    public void setBankrupt(boolean bankrupt) {
        this.bankrupt = bankrupt;
    }
    @Override
    public void run() {
        handler.start();
        rpc.start();

        try {
            while (running) {
                if(this.getCurrentValue()<0){
                    askForHelp();
                    if(isBankrupt()){
                        System.out.println("BANKRUPT!!!!!!!!!!");
                        running = false;
                    }
                    else {
                        System.out.println("RESCUE !!!!!!!!!");
                    }
                }
                Socket client = serverSocket.accept();
                this.clientHandler = new ClientHandler(client, this);
                this.clientHandler.start();
            }
            System.out.println("Bank stop working");
        } catch (Exception ignored) {
        }


    }

    private void askForHelp() {
        System.out.println("SENDING HELPING REQUEST");
        for (Map.Entry<String, Integer> entry : rpcAdress.entrySet()) {
            String hostRpc = entry.getKey();
            int portRpc = entry.getValue();

            try{
                TTransport transport;
                transport = establishConnection(hostRpc, portRpc);
                TProtocol protocol = new TBinaryProtocol(transport);
                Client client = new Client(protocol);
                Double value = Double.valueOf(-this.getCurrentValue());
                LoanRequest request = new LoanRequest(value);
                LoanResponse response = client.requestLoan(request);
                System.out.println("Response: "+ response);
                if(response.equals(LoanResponse.APPROVED)){
                    System.out.println(hostRpc+ " success to rescue");
                    this.setCurrentValue(10000);
                    break;
                }
                else if(response.equals(LoanResponse.DENIED)){
                    System.out.println(hostRpc+" fail to rescue");
                    setBankrupt(true);
                }
                else {
                    System.out.println("Else case: "+ response);
                }
            }
            catch (Exception e){
                System.out.println("Error");
                e.printStackTrace();
            }

        }

    }

    public HashMap<Code, Message> getSavedMessage() {
        return savedMessage;
    }

    public int getPort() {
        return this.port;
    }

    public void addSavedMessage(Code code, int quantity, int price) {
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

    public int getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(int value) {
        this.currentValue = value;
    }
    @Override
    public TSocket establishConnection(String host, int port) {
        TSocket socket = null;
        while (socket == null) {
            try {
                socket = new TSocket(host, port);
            } catch (Exception ignored) {
            }
        }
        while (!socket.getSocket().isConnected())
            try {
                socket.open();
            } catch (Exception e) {
                System.err.println("Error opening socket");
            }
        return socket;
    }



}
