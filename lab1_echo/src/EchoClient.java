import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Created by Xian on 2016-01-10.
 */
public class EchoClient {
    public static void main(String[] args) throws IOException{
        Socket socket=new Socket("localhost", 9876);

        BufferedReader reader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
        DataOutputStream writer=new DataOutputStream (socket.getOutputStream());


        String result= reader.readLine();
        System.out.println(result);
        BufferedReader buffer_in = new BufferedReader(new InputStreamReader(System.in));
        //String userInput = buffer_in.readLine();
        String userInput=" ";
        while(!userInput.equals("quit")){
            userInput=buffer_in.readLine();
            long starttime=System.currentTimeMillis();
            writer.writeBytes(userInput+"\r\n");
            result=reader.readLine();
            System.out.println(result);
            long endtime=System.currentTimeMillis();
            System.out.println("The response time is : "+(endtime-starttime) +"ms");
            //userInput=buffer_in.readLine();

        }
        //result=reader.readLine();
        //System.out.println(result);

        socket.close();


    }
}
