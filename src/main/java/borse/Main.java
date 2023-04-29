package borse;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Serves as the entrypoint to the Gateway.
 * Parses the arguments passed in the docker-compose file
 * into the corresponding addresses and ports.
 */
public class Main {
    public static void main(String[] args) throws IOException {
        //if (args.length != )
            //throw new Error("Gateway needs exactly 7 arguments!");
        int handlerCount = 3;
        BankHandler[] handlers = new BankHandler[handlerCount];
        DatagramSocket receiver = new DatagramSocket();
        int bankID = 1;
        handlers[0]= new BankHandler(InetAddress.getByName("bank" + bankID++),
            1234,
            receiver);
        handlers[1]= new BankHandler(InetAddress.getByName("bank" + bankID++),
            1235,
            receiver);
        handlers[2]= new BankHandler(InetAddress.getByName("bank" + bankID++),
            1236,
            receiver);
        Borse borse= new Borse("borse1",handlers);
        borse.startPullingData(3000);
    }
}
