/**
 * Created by xian on 07/02/2016.
 */

import com.sun.org.apache.xpath.internal.SourceTree;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;


public class SAWClient {
    public static void main( String[] args) throws IOException {
        Socket clientSocket = new Socket(args[0],Integer.parseInt(args[1]));
        BufferedReader reader =new BufferedReader(new InputStreamReader( clientSocket.getInputStream()));
        DataOutputStream writer = new DataOutputStream(clientSocket.getOutputStream());

        Scanner scanner = new Scanner(System.in);
        String hello= reader.readLine();
        System.out.println(hello);


        int packetNum = scanner.nextInt();
        writer.writeBytes(packetNum + "\r\n");
        System.out.println("sending "+ packetNum + " of packets.");
        int packet=1;
        for(packet=1;packet<=packetNum;packet++){
            writer.writeBytes(packet+"\r\n");
            System.out.println("sent packet : " +packet);
            String ack=null;
            ack=reader.readLine();
            System.out.println("acknowledgement number received: "+ack);
            while(Integer.parseInt(ack)!=packet){
                System.out.println("acknowledge number is not correct. redo transmission");
                writer.writeBytes(packet+"\r\n");
                ack=reader.readLine();
                System.out.println("new acknowledgement number received: "+ack);
            }

        }
        clientSocket.close();
        System.out.println("socket closed.");
    }
}
