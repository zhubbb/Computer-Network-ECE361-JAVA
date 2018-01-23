import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by xian on 13/01/2016.
 */
public class ChatServer2 {
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
                Connection2 connection = new Connection2(clientSocket);
                Thread thread = new Thread(connection);
                thread.start();

               
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

class Connection2 implements Runnable{
    DataOutputStream clientWriter=null;// =new DataOutputStream()
    BufferedReader clientReader=null;
    BufferedReader inputStream=null;
    Socket clientSocket=null;
    ExecutorService executor=null;
    String clientId=null;
    public Connection2(Socket socket) throws IOException {
        clientSocket=socket;
        clientReader=new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        clientWriter=new DataOutputStream(clientSocket.getOutputStream());
        inputStream=new BufferedReader(new InputStreamReader(System.in));
        clientId=clientSocket.getRemoteSocketAddress().toString();
    }

    public void nonthread_reading() throws IOException {

        while (true) {
            String clientResponse = clientReader.readLine();
            if(state[0]==State.Closed){
                return;
            }
            System.out.println("Client " + clientId  + " reply: \n " + clientResponse);
            if(clientResponse.equals("quit")) {
                clientWriter.writeBytes("QUIT!!!" + "\r\n");
                clientWriter.flush();
                //clientSocket.close();
                return;
            }


        }
    }

     public static  enum State{
        Running,
        Closed;
    }


    final State[] state = {State.Running};
    Runnable startWriting = new Runnable() {

        @Override
        public void run() {
            try {
                String serverReply = null;
                while(state[0]==State.Running) {

                    serverReply = inputStream.readLine();
                    if(state[0]==State.Closed){
                        return;
                    }
                    if(serverReply.equals("quit")) {
                        clientWriter.writeBytes("QUIT!!!" + "\r\n");
                        //clientWriter.flush();
                        //clientSocket.close();
                        state[0]=State.Closed;
                        //clientSocket.close();
                        return;
                    }
                    clientWriter.writeBytes(serverReply + "\r\n");
                    //clientWriter.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };
    Runnable startReading = new Runnable() {

        @Override
        public void run() {



                try {
                    String clientResponse = null;
                    while(state[0]==State.Running) {
                        clientResponse = clientReader.readLine();
                        System.out.println("Client " + clientId + " reply: \n " + clientResponse);
                        if (clientResponse.equals("quit")) {
                            clientWriter.writeBytes("QUIT!!!" + "\r\n");
                            //clientWriter.flush();
                            //clientSocket.close();
                            state[0] = State.Closed;

                            return;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

        }
    };




    @Override
    public void run() {
        try{
            clientWriter.writeBytes("Welcome to chat server!!!" + "\r\n");


            executor = Executors.newCachedThreadPool();
            executor.submit(startWriting);
            //executor.submit(writing);
            nonthread_reading();
            executor.shutdown();
                try{
                executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                clientSocket.close();
            }


            //new Thread(startWriting).start();
            //new Thread(startReading).start();
            //nonthread_reading(); //we can use the original thread(main thread) as reading thread ,but it neeed to be below writing since it is a while loop
            //clientSocket.close();
            System.out.println("connection to " + clientId  + " closed.");
        } catch (IOException e){
            e.printStackTrace();
        }

    }

}