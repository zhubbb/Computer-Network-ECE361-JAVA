import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Xian on 2016-02-08.
 */
public class GBNClient3 {
    public static AtomicInteger lastAck = new AtomicInteger(0);
    public static void setLastAck(int AckNum){
        GBNClient3.lastAck.set(AckNum);
    }
    public static void main(String[] args) throws IOException {
        Socket clientSocket = new Socket(args[0],Integer.parseInt(args[1]));


        Listener2 listener= new Listener2(clientSocket);
        Thread thread = new Thread(listener);
        thread.start();

        BufferedReader reader =new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        DataOutputStream writer = new DataOutputStream(clientSocket.getOutputStream());
        Scanner scanner = new Scanner(System.in);

        System.out.println("what is total number of packets to be sent?");
        int packetNum = scanner.nextInt();
        writer.write(packetNum);
        System.out.println("what is the probability of lost?");
        int probability = scanner.nextInt();
        writer.write(probability);

        System.out.println("what is the window size?");
        int wsize = scanner.nextInt();
        System.out.println("what is the maximum timeout?");
        int timeout = scanner.nextInt();


        //packet is sent+1 = the packet to be sent next
        int packet=1;
        int sent=0;
        long timer[]=new long[wsize];

        //lastAck+1 is the initial of window
        long time1=System.currentTimeMillis();
        while(lastAck.get()<packetNum){
            if(lastAck.get()>sent){
                System.out.println("acknowledge number is outside bound.");
                System.out.println("resending.");
                packet=sent-wsize+1;
                sent=packet-1;
            }
            if(sent<wsize+lastAck.get()&&sent<packetNum){
                writer.write(packet);
                packet++;
                sent++;
                timer[(sent)%wsize]=System.currentTimeMillis();
                System.out.println("sent packet : " +sent);
            }
            else if(System.currentTimeMillis()-timer[(lastAck.get()+1)%wsize]>timeout){
                System.out.println("time out for packet : " +(sent-wsize+1));
                System.out.println("resending.");
                packet=lastAck.get()+1;
                sent=packet-1;

            }


        }
        long time2=System.currentTimeMillis();
        while(thread.isAlive()) {
            //wait

        }
        System.out.println("Total time spent is : " + (time2 - time1) + " ms.");
        return;

    }
}
class Listener2 implements Runnable{

    Socket clientSocket=null;
    BufferedReader reader=null;
    Scanner scanner=null;
    public Listener2(Socket socket) throws  IOException{
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
            if (AckNum > GBNClient3.lastAck.get()) {
                GBNClient3.setLastAck(AckNum);
                System.out.println("Received Ack num : "+AckNum);
            }else if(AckNum==-1){
                System.out.println("submission end.");
                END=true;
            }else{
                System.out.println("received: "+AckNum +" acknowledge number is not correct. aborted");

            }
        }


    }
}

//Q: why at least wSize packets can be sent in step 3? Give an example that it could be more than wSize.
// initially the packet can be sent from packet 1 to packet wsize since the window contain 0-wsize packets.
//if the server reply during sending and slides the window, then it can send more.