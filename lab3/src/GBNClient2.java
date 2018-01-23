import com.sun.org.apache.xpath.internal.operations.Bool;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by xian on 08/02/2016.
 */
public class GBNClient2 {
    public static AtomicInteger lastAck = new AtomicInteger(0);
    public static void setLastAck(int AckNum){
        GBNClient2.lastAck.set(AckNum);
    }
    public static void main(String[] args) throws IOException {
        Socket clientSocket = new Socket(args[0],Integer.parseInt(args[1]));


        Listener listener= new Listener(clientSocket);
        new Thread(listener).start();

        BufferedReader reader =new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        DataOutputStream writer = new DataOutputStream(clientSocket.getOutputStream());
        Scanner scanner = new Scanner(System.in);

        System.out.println("what is total number of packets to be sent?");
        int packetNum = scanner.nextInt();
        writer.write(packetNum);
        System.out.println("what is the probability of lost?");
        int probability = scanner.nextInt();
        writer.write(probability);

        int packet=1;
        int sent=0;

        while(sent<packetNum){
            if(sent-lastAck.get()<1 && sent-lastAck.get()>=0){
                packet=lastAck.get()+1;
                writer.write(packet);
                sent++;
                System.out.println("sent packet : " +packet);
            }


        }



    }
}
class Listener implements Runnable{

    Socket clientSocket=null;
    BufferedReader reader=null;
    Scanner scanner=null;
    public Listener(Socket socket) throws  IOException{
        clientSocket=socket;
        reader =new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        scanner = new Scanner(System.in);

    }

    @Override
    public void run() {
        try {
            startrunning();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
    public void startrunning() throws IOException{

        Boolean END=false;
        while(!clientSocket.isClosed()&&!END) {
            int AckNum = reader.read();
            if (AckNum > GBNClient2.lastAck.get()) {
                GBNClient2.setLastAck(AckNum);
                System.out.println("Received Ack num : "+AckNum);
            }else if(AckNum==-1){
                System.out.println("submission end.");
                END=true;
            }else{
                System.out.println("acknowledge number is not correct.");

            }
        }


    }
}