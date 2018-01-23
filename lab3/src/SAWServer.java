import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.io.*;
import java.net.Socket;


/**
 * Created by xian on 07/02/2016.
 */
public class SAWServer {
    public static void main(String args[]) throws IOException{
        int port =Integer.parseInt(args[0]);
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Xian's Server is running\nHost name: "
                + InetAddress.getLocalHost().getHostName() + "\nHost Address: "
                + InetAddress.getLocalHost().getHostAddress() + ":" + port + "\nwaiting for requests.");
        while(true) {
            Connection_SAWServer connection_sawServer=new Connection_SAWServer(serverSocket.accept());
            connection_sawServer.start();
            //clientSocket=serverSocket.accept();
            //Connection2 connection2 = new Connection2(clientSocket);
            //new Thread(connection2).start();
        }

    }
}
//or use implements Runnable
class Connection_SAWServer extends Thread{
    DataOutputStream clientWriter = null;// =new DataOutputStream()
    BufferedReader clientReader = null;
    BufferedReader inputStream = null;
    Socket clientSocket = null;
    String clientId = null;
    public Connection_SAWServer(Socket socket) throws IOException {
        clientSocket = socket;
        clientReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        clientWriter = new DataOutputStream(clientSocket.getOutputStream());
        inputStream = new BufferedReader(new InputStreamReader(System.in));
        clientId = clientSocket.getRemoteSocketAddress().toString();

    }


    public void run(){
        try{
            startrunning();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            System.out.println("connection to " + clientId + " closed.");
        }

    }

    public void startrunning() throws IOException {
        clientWriter.write("Welcome to Store and wait server!!!\r\n".getBytes());
        int packetNum=Integer.parseInt(clientReader.readLine());
        System.out.println("Receiving "+ packetNum +" packets.");
        int lastACK=0;
        int received=0;
        while(lastACK<packetNum){
            received=Integer.parseInt(clientReader.readLine());
            System.out.println("Received packet: "+received+" from client: "+clientId);
            if(received== lastACK+1) {
                clientWriter.writeBytes(received+"\r\n");
                lastACK ++;
            }
        }

    }

}