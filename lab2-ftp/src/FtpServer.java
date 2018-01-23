
import java.net.*;
import java.io.*;
import java.sql.Connection;

/**
 * Created by Xian on 2016-01-20.
 */
public class FtpServer {
    private static int port;
    private static ServerSocket serverSocket;

    public static void main(String[] args) {
        Socket clientSocket = null;
        port = 9876;
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Xian's Server is running\nHost name: "
                    + InetAddress.getLocalHost().getHostName() + "\nHost Address: "
                    + InetAddress.getLocalHost().getHostAddress() + ":" + port + "\nwaiting for requests.");
            while(true) {
                clientSocket=serverSocket.accept();
                Connection1 connection1 = new Connection1(clientSocket);
                new Thread(connection1).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


    }



}
class Connection1 implements Runnable {
    DataOutputStream clientWriter = null;// =new DataOutputStream()
    BufferedReader clientReader = null;
    BufferedReader inputStream = null;
    Socket clientSocket = null;
    String clientId = null;
    private byte[] packet=new byte[1024];
    private int length=0;


    public Connection1(Socket socket) throws IOException {
        clientSocket = socket;
        clientReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        clientWriter = new DataOutputStream(clientSocket.getOutputStream());
        inputStream = new BufferedReader(new InputStreamReader(System.in));
        clientId = clientSocket.getRemoteSocketAddress().toString();
    }

    public void start() throws IOException {
        clientWriter.write("Welcome to FTP server!!!\r\n".getBytes());
        while (true) {
            String clientResponse = clientReader.readLine();
            System.out.println("Client " + clientId + " reply: \n " + clientResponse);
            if(clientResponse !=null){
                if(clientResponse.equals("quit")){
                    clientWriter.write("000".getBytes());
                    break;
                }
                File fileOnServer= new File(clientResponse);
                if(!fileOnServer.exists()){
                    System.out.println("File:"+"\""+clientResponse+"\" Not found.");
                    clientWriter.write("303".getBytes());
                }
                else{
                    java.io.FileInputStream fin= new FileInputStream(fileOnServer);
                    clientWriter.write("200".getBytes());
                    long filelength=fileOnServer.length();

                    System.out.println("File:"+"\""+clientResponse+"\" found.\nstarting uploading.");
                    System.out.println("file length is : "+ filelength);
                    while(true){
                        packet=new byte[1024];
                        length=fin.read(packet);
                        if(length==-1){
                            System.out.println("Finishing file transmission: "+clientResponse);
                            fin.close();
                            break;
                        }
                        //System.out.println(new String(packet, "UTF-8"));
                        else{
                            //System.out.println(length);
                            clientWriter.write(packet,0,length);
                        }



                    }
                }
            }
        }
    }

    @Override
    public void run() {
        try {
            start();
            clientSocket.close();
            System.out.println("connection to " + clientId + " closed.");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}