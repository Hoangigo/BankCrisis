package bank;

import connection.Establisher;
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
import thrift.LoanRequest;
import thrift.LoanResponse;

public class RPCHelper extends Thread implements BankService.Iface{
  private TServer server;
  private Bank bank;
  public RPCHelper(Bank bank){
    this.bank= bank;
  }
  @Override
  public void run(){
    startServer();
  }
  public void startServer(){
    String temp = System.getenv("RPCPORT");
    if(temp != null) {
      int rpcPort = Integer.parseInt(temp);
      TServerTransport transport = null;
      try {
        transport = new TServerSocket(rpcPort);
        server = new TSimpleServer(
            new TServer.Args(transport).processor(new BankService.Processor<>(this)));
        server.serve();
      } catch (TTransportException e) {
        e.printStackTrace();
      }

    }
  }
  @Override
  public LoanResponse requestLoan(LoanRequest request) throws TException {
    if(this.bank.getCurrentValue()> request.getAmount()) {
      this.bank.setCurrentValue(this.bank.getCurrentValue()- (int)request.getAmount());
      return LoanResponse.APPROVED;
    }
    return LoanResponse.DENIED;

  }


}
