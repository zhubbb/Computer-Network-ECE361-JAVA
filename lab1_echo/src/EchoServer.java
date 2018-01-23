/**
 * Created by Xian on 2016-01-10.
 */
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;




public class EchoServer{

    public static enum Mode{
        Verbose,
        Silent;


    }

    public static enum ServiceType{
        Echo_server;

    }

    public static void main(String[] args){

        int port = 9876;
        long client_id=1;
        ServerSocket server_socket;
        try {
            server_socket = new ServerSocket(port);
            System.out.println("Server online.\nHost name: " + InetAddress.getLocalHost().getHostName() + "\nHost Address: " + InetAddress.getLocalHost().getHostAddress() + ":" + port + "\nwaiting for requests.");

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("could not open the server socket on the given port.");
            return;
        }
        while(true){
            try{
                Socket client_socket= server_socket.accept();
                System.out.println("request received."+ " client: " + client_socket.getRemoteSocketAddress().toString());
                Connection connection= new Connection(client_socket,client_id,EchoServer.ServiceType.Echo_server, EchoServer.Mode.Verbose);
                Thread thread=new Thread(connection);
                thread.start();
                //connection.run();
                ++client_id;
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}










class Connection implements Runnable{
    private BufferedReader reader;
    private DataOutputStream writer;
    private EchoServer.Mode mode;
    private Socket socket;
    private final long client_id;
    EchoServer.ServiceType serviceType;//default accesstype is private

    public Connection(Socket socket, long client_id, EchoServer.ServiceType serviceType, EchoServer.Mode mode)throws IOException {

        this.mode = mode;
        this.socket = socket;
        this.client_id = client_id;
        this.reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        this.writer = new DataOutputStream(this.socket.getOutputStream());
        this.serviceType=serviceType;
    }
    public void start() throws IOException {
        writeline("Welcome to ECE361 Sercer. ServerType: Echo_Server.");
        String line=" ";
        while (true){
            line = this.readline();
            if(line.equalsIgnoreCase("quit")){
                this.writeline("Quit!!!");
                return;
            }
            else{
                this.writeline(line);
            }
        }
    }



    private void writeline(String line) throws IOException{
        this.writer.writeBytes(line+"\r\n");
        this.writer.flush();
        if(this.mode==EchoServer.Mode.Verbose){
            System.out.println("to "+ this.client_id + ": " +line);
        }
    }
    public String readline() throws IOException{
        String line=this.reader.readLine();
        if(this.mode== EchoServer.Mode.Verbose){
            System.out.println("from "+ this.client_id + ": " +line);
        }
        return line;
    }

    @Override
    public void run() {
        try {
            start();
            socket.close();
            if(this.mode == EchoServer.Mode.Verbose) {
                System.out.println("connection to " + this.client_id + " closed.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("connection to client: " + this.client_id+ " end unexpectedly.");

        }
    }
}
