import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

import static jdk.nashorn.internal.objects.NativeMath.min;

/**
 * Created by Bob on 2016-02-06.
 * a- Use Scanner to read the number of packets from the user. This can be done as
 * Scanner scr = new Scanner(System.in);
 * Use nextInt() method of scanner to read the number of packets from the user, and store it in a variable, noPackets.
 * b- Send that number to the server
 * c- Define another variable, sent, that keeps track of the packet in the sliding window. Initially, sent is set to 1
 * d- Send the packet number (sent) to the server and wait for an acknowledgment.
 * e- Read the acknowledgement from the server, if the received number is equal to sent, slide the window by one position, i.e. sent = sent + 1.
 * f- Continue the process until all packets have been sent.
 */
public class GBNClient {
  static AtomicInteger lastAck = new AtomicInteger(0);


  public static void setLastAck(int lastAck) {
    GBNClient.lastAck.set(lastAck);
  }

  public static int getLastAck(){
    return lastAck.get();
  }

  public static void main(String[] args) throws IOException, InterruptedException {

    Socket socket = new Socket(args[0], Integer.parseInt(args[1]));
    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    DataOutputStream writer = new DataOutputStream(socket.getOutputStream());
    AckListenerThread ackListenerThread = new AckListenerThread(socket);
    ackListenerThread.start();
    Scanner scr = new Scanner(System.in);
    //read user input
    System.out.println("pls enter num of packet");
    int noPackets = scr.nextInt();
    System.out.println("pls enter prob error");
    int probError = scr.nextInt();
    System.out.println("pls enter window size");
    int wSize = scr.nextInt();
    System.out.println("pls enter time out");
    int timeOut = scr.nextInt();

    long timer[] = new long[wSize];


    writer.write(noPackets);
    writer.write(probError);
    //System.out.println("Number of packet is " + noPackets);
    //Q: at least
    //if the server reply with ack, then the window size increase
    int sent = 1;
        /*for (int i = 0; i <= min(wSize+lastAck, noPackets); i++) {
            timer[(sent-1)%wSize] = System.currentTimeMillis();
            writer.write(sent);
            sent++;
            //String temp;
            //temp=reader.readLine();
            //System.out.println("recieved: "+temp);
            //while(Integer.parseInt(temp)!=packet) {
            //    temp=reader.readLine();
            //    System.out.println("recieved retry: "+temp);
            //}
        }*/
    long firstTime = System.currentTimeMillis();
    while (lastAck.get() < noPackets) {
      long currentTime = System.currentTimeMillis();
      if (currentTime - timer[lastAck.get() % wSize] > timeOut) {//sent - 1 is lastAck
        //time out
        int resent = lastAck.get() + 1;
        //writer.write(resent);
        //timer[(resent-1) % wSize] = System.currentTimeMillis();
        sent = resent;
        System.out.println("resending whole window...");
      }
      if (sent - lastAck.get() <= wSize && sent <= noPackets) {
        timer[(sent - 1) % wSize] = System.currentTimeMillis();
        writer.write(sent);
        System.out.println("sent: " + sent);
        sent++;
      }
    }

    while (true) {
      //System.out.println("last ack:"+ lastAck);
      if (!ackListenerThread.isAlive()) {

        long lastTime = System.currentTimeMillis();
        System.out.println("The total time take is: " + (lastTime - firstTime));
        socket.close();
        return;
      }
    }


  }

}

class AckListenerThread extends Thread {
  Socket socket = null;

  AckListenerThread(Socket socket) {
    this.socket = socket;
  }

  @Override
  public void run() {
    try {
      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      while (!socket.isClosed()) {
        int ack = bufferedReader.read();
        if(ack != -1 && ack <= GBNClient.getLastAck()){
          System.out.println("drop acknowledgement: "+ack);
          continue;
        }
        if (ack != -1) {
          GBNClient.setLastAck(ack);
          System.out.println("ack recieved: " + ack);
        } else {
          break;
        }
      }

    } catch (IOException e) {
      e.printStackTrace();
    }

  }
}