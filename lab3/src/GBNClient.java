import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by xian on 08/02/2016.
 */
public class GBNClient {
    public static void main(String[] args) throws IOException{
        Socket clientSocket = new Socket(args[0],Integer.parseInt(args[1]));
        BufferedReader reader =new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        DataOutputStream writer = new DataOutputStream(clientSocket.getOutputStream());
        Scanner scanner = new Scanner(System.in);


        System.out.println("what is total number of packets to be sent?");
        int packetNum = scanner.nextInt();
        writer.write(packetNum);
        System.out.println("what is the probability of lost?");
        int probability = scanner.nextInt();
        writer.write(probability);

        for(int packet=1;packet<=packetNum;packet++){
            writer.write(packet);
            System.out.println("sent packet : " +packet);
        }
        while(reader.read()!=-1) {//keep reading until read -1
        }
        if(reader.read()==-1) {
            clientSocket.close();
        }

    }
}
