package coordinator;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class Coordinator {
  private static final String CLIENT_ID = "coordinator";
  private static final String HELP_TOPIC = "help";
  private static final String PREPARE_TOPIC = "prepare";
  private static final String COMMIT_TOPIC = "commit";
  private static final String FINISH_TOPIC = "finish";
  private List<String> banks;
  private String requestingBank;
  private int totalBanks;
  private int responses;
  private int approved;
  private MqttClient mqttClient;
  public Coordinator() {
    banks = new ArrayList<>();
    responses = 0;
    approved =0;
    // Initialize the list of banks
    banks.add("Bank1");
    banks.add("Bank2");
    banks.add("Bank3");
    totalBanks= banks.size();
    try {
      mqttClient =createClient("broker",1883);
      mqttClient.setCallback(new MqttCallback() {
        @Override
        public void connectionLost(Throwable cause) {
          System.out.println("Connection to MQTT broker lost!");
          cause.printStackTrace();
          // Handle reconnection if needed
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws MqttException {
          if (topic.equals(HELP_TOPIC)) {
            handleHelpRequest(message);
          } else if (topic.equals(PREPARE_TOPIC)) {
            handlePrepareMessage(message);
          } else if (topic.equals(COMMIT_TOPIC)) {
            handleCommitMessage(message);
          }
          else if (topic.equals(FINISH_TOPIC)) {
            handleFinishMessage(message);
          }
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
          // Not needed for subscriber
        }
      });

      mqttClient.connect();
      System.out.println("Coordinator connected to the MQTT broker.");

      // Subscribe to the relevant topics
      mqttClient.subscribe(HELP_TOPIC);
      mqttClient.subscribe(PREPARE_TOPIC);
      mqttClient.subscribe(COMMIT_TOPIC);
      mqttClient.subscribe(FINISH_TOPIC);
    } catch (MqttException e) {
      e.printStackTrace();
    }
  }
  public void start(){
    System.out.println("Coordinator start");
    while (true) {
    }
  }

  private void handleFinishMessage(MqttMessage message) {

  }

  private void handleCommitMessage(MqttMessage message) {
    String commitRequest = new String(message.getPayload());
    String bank = commitRequest;

    System.out.println("Received commit message from " + bank);
    responses++;
    if (responses == totalBanks - 1) {
      // Notify requesting bank of successful completion
      //sendMQTTMessage("finish", bank);
      // Reset responses counter
      responses = 0;
      System.out.println("DONE");
    }
  }

  private void handlePrepareMessage(MqttMessage message) {

    System.out.println("Handle Prepare Message from Coordinator");
    String prepareRequest = new String(message.getPayload());
    String[] prepareParts = prepareRequest.split(";");
    String bankName = prepareParts[0];
    String vote = prepareParts[1];
    if(vote.equals("true")) approved++;
    System.out.println("Received prepare message from " + bankName + " with vote " + vote);
    responses++;
    if(responses== totalBanks-1){
      System.out.println("All Banks agree");
      sendMQTTMessage("commit",bankName);
      this.approved=0;
      this.responses=0;
    }
    else{
      System.out.println("ABORT not all agree");
      sendMQTTMessage("commit","abort");

    }


  }

  private void handleHelpRequest(MqttMessage message) throws MqttException {
    System.out.println("coordinator start prepare process");
    mqttClient.publish("prepare", message);

  }
  private void sendMQTTMessage(String topic, String mess){
    System.out.println("Send mqtt from Coordinator with message "+ mess +"with topic "+ topic );
    try {
      MqttMessage msg = new MqttMessage();
      msg.setPayload(mess.getBytes());
      msg.setQos(0);
      msg.setRetained(true);
      mqttClient.publish(topic,msg);
    } catch (MqttException ignore) {
    }
  }

  public MqttClient createClient(String host,int port){
    MqttClient publisher = null;
    String id = UUID.randomUUID().toString();
    while (publisher== null){
      try {
        String ip = InetAddress.getByName(host).toString().substring(host.length()+1);
        publisher= new MqttClient("tcp://"+ip+":"+port,id);

      } catch (UnknownHostException e) {
        throw new RuntimeException(e);
      } catch (MqttException e) {
        throw new RuntimeException(e);
      }
    }
    while(!publisher.isConnected()){
      try {
        publisher.connect();
      } catch (MqttException e) {
        throw new RuntimeException(e);
      }
    }
    return publisher;

  }

}
