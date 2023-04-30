package client;

import borse.Code;
import connection.Establisher;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

import java.util.HashMap;

public class Client extends Thread implements Establisher<Socket> {
  private int money ;
  HashMap<Code, Integer> savedProperties;
  private BufferedReader reader;
  private OutputStream writer;
  private final Socket connection[];
  private String servers[];
  private boolean running ;

  @Override
  public void run(){
    while(running){
      try {
        this.codeRequest("bank1",Code.LSFT,10);
        this.getServerResponse();
        this.codeRequest("bank2",Code.ABCD,50);
        this.getServerResponse();
        this.codeRequest("bank3",Code.EFGH,100);
        this.getServerResponse();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }


  }
  public boolean isAccepted() {
    return accepted;
  }

  public void setAccepted(boolean accepted) {
    this.accepted = accepted;
  }
  public void addProperty(Code code, int quantity){
    Integer amount = savedProperties.get(code);
    if (amount == null) {
      amount = quantity;
      savedProperties.put(code, amount);
    } else {
      int newQuantity = amount + quantity;
      savedProperties.put(code, newQuantity);

    }
  }

  private boolean accepted;

  public int getMoney() {
    return money;
  }

  public void setMoney(int money) {
    this.money = money;
  }

  public Client(String servers[], int ports[]) throws IOException {
    this.servers= new String[servers.length];
    for(int i =0; i < servers.length;i++){
      this.servers[i]= servers[i];
    }
    this.connection = new Socket[servers.length];
    for(int i = 0 ; i < servers.length;i++){
      //this.connection[i]= new Socket(InetAddress.getByName(servers[i]), ports[i]);
      this.connection[i]= establishConnection(servers[i], ports[i]);
    }
    this.money =0;
    this.accepted= false;
    this.running = true;

  }
  private int findServerPosition(String server){
    int pos =-1;
    for(int i =0; i<servers.length;i++){
      if(server.equals(servers[i])){
        pos = i;
      }
    }
    return pos;
  }
  private void sendPostRequest(String server,String message) throws IOException {
    int pos = findServerPosition(server);
    this.reader = new BufferedReader(new InputStreamReader(this.connection[pos].getInputStream()));
    this.writer = this.connection[pos].getOutputStream();
    String postRequest = "POST / HTTP/1.1\r\n"
        + "Host: Bank 100\r\n"
        + "Content-Type: text/plain\r\n"
        + "Content-Length: " + message.length()+ "\r\n"
        +"\r\n" + message;
    writer.write(postRequest.getBytes());
  }
  private void getResponseFromBank() throws IOException {
    String line;
    while (!(line = reader.readLine()).equals(""))
      System.out.println(line);
  }
  public void moneyRequest(String server,int money) throws IOException {
    sendPostRequest(server, "money: "+money);
  }
  public void codeRequest(String server,Code code,int quantity) throws IOException {
    sendPostRequest(server,"code: "+code.toString() + "\r\n"+ "quantity: "+ quantity);
  }


  @Override
  public Socket establishConnection(String host, int port) {
    Socket tmp = null;
    while (tmp == null) {
      try {
        tmp = new Socket(InetAddress.getByName(host), port);
      } catch (Exception e) {
        System.out.println("Error");
        System.out.println(e.getMessage());
      }
    }
    return tmp;  }
  public void getServerResponse() throws IOException {
    String line;
    while (!(line = reader.readLine()).equals(""))
      System.out.println(line);
  }
}
