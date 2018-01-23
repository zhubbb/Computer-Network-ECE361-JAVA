import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Bob on 2016-02-25.
 */
public class TCPCongestionControlClient {
    private static AtomicInteger lastAck = new AtomicInteger(0);
    protected static double cwnd = 1.0;
    protected static int ssthresh = 8;
    protected static int RTT = 1000;
    protected static int timeOut = 1010;

    public static int getLastAck() {
        return lastAck.get();
    }

    public static void setLastAck(int lastAck) {
        synchronized (TCPCongestionControlClient.lastAck) {
            int delta = lastAck - TCPCongestionControlClient.lastAck.get();
            TCPCongestionControlClient.lastAck.set(lastAck);
            //cwnd increases after get the ack

            if (cwnd < ssthresh) {
                TCPCongestionControlClient.cwnd+=delta;
            } else {
                TCPCongestionControlClient.cwnd += delta / (double) ((int)(double) cwnd);
            }
        }
    }


    public static void main(String[] args) throws IOException {
        Socket socket = new Socket(args[0], Integer.parseInt(args[1]));
        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
        Scanner scanner = new Scanner(System.in);
        System.out.println("Please enter number of packets:");
        int noPackets = scanner.nextInt();
        AckListenerThread ackListenerThread = new AckListenerThread(socket, noPackets);
        ackListenerThread.start();

        long timer[] = new long[noPackets];
        int sent = 1;

        dataOutputStream.write(noPackets);

        //start to send
        long firstTime = System.currentTimeMillis();
        timer[lastAck.get()] = firstTime;
        while (true) {
            synchronized (lastAck) {
                if (lastAck.get() < noPackets) {
                    long currentTime = System.currentTimeMillis();
                    if(sent > lastAck.get()+1) {
                        if (currentTime - timer[lastAck.get()] > timeOut) {
                            //time out
                            System.out.println(currentTime - timer[lastAck.get()] + " timer");
                            int resent = lastAck.get() + 1;
                            sent = resent;

                            //change cwnd and ssthresh
                            ssthresh = ((int) (double) cwnd) / 2;
                            cwnd = 1.0;
                            System.out.println("timeout!!!, ssthresh =" + ssthresh + " cwnd = "+ cwnd);
                            System.out.println("resending whole window...");
                        }
                    }
                    if (sent - lastAck.get() <= (int)(double) cwnd && sent <= noPackets) {
                        timer[(sent - 1)] = System.currentTimeMillis();
                        try {
                            dataOutputStream.write(sent);
                        }
                        catch (Exception e){

                        }
                        System.out.println("cwnd="+ cwnd);
                        System.out.println("sent: " + sent);
                        sent++;
                    }
                } else {
                    break;
                }
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
    int noPacket = 0;
    AckListenerThread(Socket socket, int noPacket) {
        this.socket = socket;
        this.noPacket = noPacket;
    }

    @Override
    public void run() {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            while (!socket.isClosed()) {
                int ack = bufferedReader.read();

                if (ack != -1 && ack <= TCPCongestionControlClient.getLastAck()) {
                    System.out.println("drop acknowledgement: " + ack);
                    continue;
                } else if (ack != -1) {
                    TCPCongestionControlClient.setLastAck(ack);
                    System.out.println("ack recieved: " + ack);
                } else {
                    break;
                }
            }

        } catch (IOException e) {
            //e.printStackTrace();
        }

    }
}
