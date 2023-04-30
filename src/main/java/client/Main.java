package client;

import borse.Code;
import java.io.IOException;

public class Main {
  public static void main(String[] args) throws IOException {
    String[] servers = {args[0],args[1],args[2]};
    int ports[] = {Integer.parseInt(args[3]),Integer.parseInt(args[4]),Integer.parseInt(args[5])};
    Client client = new Client(servers,ports);
    client.start();
    /*
    client.codeRequest("bank1",Code.LSFT,10);
    client.codeRequest("bank2",Code.ABCD,50);
    client.codeRequest("bank3",Code.EFGH,100);
    client.getServerResponse();*/
/*
    if(client.isAccepted()){
      client.addProperty(Code.LSFT,100);
      System.out.println("properties added");
      client.setAccepted(false);
    }

    client.moneyRequest("bank1",100);
    if(client.isAccepted()){
      client.setMoney(10000);
      System.out.println("I have 100$");
      client.setAccepted(false);
    }*/
  }

}
