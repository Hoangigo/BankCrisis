package bank;

import java.io.IOException;


public class Main {
    public static void main(String[] args) throws IOException {
        //Bank udpsensor = new Bank(args[0],args[1],Integer.parseInt(args[2]));
        //udpsensor.start();

        Bank bank = new Bank( args[0],Integer.parseInt(args[1]),"browser");
        bank.start();

    }
}
