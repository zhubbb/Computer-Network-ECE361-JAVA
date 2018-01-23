import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Xian on 2016-01-12.
 */
public class ChatServer1 {
    public static void main(String[] args) {
        int port = 9876;
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server online.\nHost name: " + InetAddress.getLocalHost().getHostName() + "\nHost Address: " + InetAddress.getLocalHost().getHostAddress() + ":" + port + "\nwaiting for requests.");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Server socket failed");
        }
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                Connection1 connection = new Connection1(clientSocket);
                Thread thread = new Thread(connection);
                thread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }
}

class Connection1 implements Runnable{
    DataOutputStream clientWriter=null;// =new DataOutputStream()
    BufferedReader clientReader=null;
    BufferedReader inputStream=null;
    Socket clientSocket=null;
    String clientId=null;
    public Connection1(Socket socket) throws IOException {
        clientSocket=socket;
        clientReader=new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        clientWriter=new DataOutputStream(clientSocket.getOutputStream());
        inputStream=new BufferedReader(new InputStreamReader(System.in));
        clientId=clientSocket.getRemoteSocketAddress().toString();
    }

    public void start() throws IOException {
        clientWriter.writeBytes("Welcome to chat server!!!" + "\r\n");
        while (true) {
            String clientResponse = clientReader.readLine();
            System.out.println("Client " + clientId  + " reply: \n " + clientResponse);
            if(clientResponse.equals("quit")) {
                clientWriter.writeBytes("QUIT!!!" + "\r\n");
                clientWriter.flush();
                //clientSocket.close();
                return;
            }

            String serverReply=inputStream.readLine();
            clientWriter.writeBytes(serverReply+"\r\n");
            clientWriter.flush();


        }
    }
    @Override
    public void run() {
        try{
            start();
            clientSocket.close();
            System.out.println("connection to " + clientId  + " closed.");
        } catch (IOException e){
            e.printStackTrace();
        }

    }

}

