/**
 * Created by xian on 28/02/2016.
 */



import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;



public class TCPclient {
    public static AtomicInteger lastAck = new AtomicInteger(0);
    protected static double cwnd = 1.0;
    protected static int ssthresh = 5;
    protected static int RTT = 1000;
    protected static int TimeOut = 1011;





    public static void setLastAck(int AckNum){
        int change = AckNum - TCPclient.lastAck.get();
        TCPclient.lastAck.set(AckNum);
        System.out.println("Received Ack num : "+AckNum);
        if (cwnd < ssthresh) {
            cwnd+=change;
            System.out.println("slow start mode, congestion window size grows to: "+cwnd );
        } else {
            TCPclient.cwnd += change / (double) ((int)(double) cwnd);
            System.out.println("congestion control mode, congestion window size grows to: "+cwnd );
        }

    }





    public static void main(String[] args) throws IOException {
        Socket clientSocket = new Socket(args[0],Integer.parseInt(args[1]));




        BufferedReader reader =new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        DataOutputStream writer = new DataOutputStream(clientSocket.getOutputStream());
        Scanner scanner = new Scanner(System.in);

        System.out.println("what is total number of packets to be sent?");
        int packetNum = scanner.nextInt();
        writer.write(packetNum);


        Listener listener= new Listener(clientSocket);
        Thread thread = new Thread(listener);
        thread.start();


        //packet is sent+1 = the packet to be sent next
        int packet=1;
        int sent=0;
        long timer[]=new long[packetNum+1];

        //lastAck+1 is the initial of window
        long time1=System.currentTimeMillis();
        while(lastAck.get()<packetNum){
            //cause the lastAck is controlled by another thread and is varing !
            //we need set another local variable to avoid it change during processing
            int lastAckCopy=lastAck.get();
            while(sent<(int)cwnd+lastAckCopy&&sent<packetNum){
                writer.write(packet);
                packet++;
                sent++;
                timer[sent]=System.currentTimeMillis();
                System.out.println("sent packet : " +sent);
            }
            //detect if the lastack is updated
            if(lastAckCopy!=lastAck.get()){
                ;
            }
            else if(System.currentTimeMillis()-timer[lastAckCopy+1]>TimeOut){

                //reset cwnd and ssthresh
                ssthresh = ((int) (double) cwnd) / 2;
                cwnd = 1.0;
                System.out.println("time out for packet : " +(lastAck.get()+1));
                System.out.println("Set ssthresh to " + ssthresh + " and set cwnd to "+ (int)cwnd);
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





class Listener implements Runnable{

    Socket clientSocket=null;
    BufferedReader reader=null;
    Scanner scanner=null;


    public Listener(Socket socket) throws  IOException{
        this.clientSocket=socket;

        reader =new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));


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
            if (AckNum > TCPclient.lastAck.get()) {
                TCPclient.setLastAck(AckNum);

            }else if(AckNum==-1){
                System.out.println("submission end.");
                END=true;
            }else{
                System.out.println("received: "+AckNum +" acknowledge number is not correct. aborted");
            }
        }


    }
}