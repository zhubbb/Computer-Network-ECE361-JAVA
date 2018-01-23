import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader;

import java.io.*;
import java.lang.reflect.Array;
import java.net.Socket;

/**
 * Created by Xian on 2016-01-21.
 */
public class FtpClient2 {
    public static void main(String[] args) throws IOException {
        Socket socket=new Socket("localhost", 9876);
        byte[] packet= new byte[1024];
        DataInputStream reader=new DataInputStream(socket.getInputStream());
        DataOutputStream writer=new DataOutputStream (socket.getOutputStream());


        int result= reader.read(packet);
        System.out.println(new String(packet,"UTF-8"));
        BufferedReader buffer_in = new BufferedReader(new InputStreamReader(System.in));
        //String userInput = buffer_in.readLine();
        String userInput=" ";

        byte[] statusCode=new byte[3];
        while(!userInput.equals("quit")){
            userInput = buffer_in.readLine();
            writer.writeBytes(userInput+"\r\n");

            result = reader.read(statusCode);
            if(new String(statusCode,"UTF-8").equals("000")){
                break;
            }
            else if(new String(statusCode).equals(new String("200".getBytes()))){
                final String finalUserInput = userInput;
                new Thread(new Runnable() {


                    @Override
                    public void run() {


                        byte[] portbyte = new byte[1024];
                        try {
                            reader.read(portbyte);

                            int dataPort = Integer.parseInt(new String(portbyte).substring(0, 4));
                            //port number read
                            System.out.println("data connection: " + dataPort + " is established.");
                            Socket dataSocket = new Socket("localhost", dataPort);
                            DataInputStream dataReader = new DataInputStream(dataSocket.getInputStream());
                            File fileOnClient = new File("newCopy_" + finalUserInput);
                            java.io.FileOutputStream fout = new FileOutputStream(fileOnClient);
                            System.out.println("Start receiving file");
                            long ct = System.currentTimeMillis();
                            while (true) {
                                int result = dataReader.read(packet);
                                if (result > 0) {
                                    fout.write(packet, 0, result);
                                }
                                if (result <= 0) {
                                    long ct2 = System.currentTimeMillis();
                                    long filelength = fileOnClient.length();
                                    fout.close();
                                    dataSocket.close();
                                    System.out.println("file submit end. file length is : " + filelength);
                                    System.out.println("\ntransmission time is : " + (ct2 - ct) + " ms");
                                    System.out.println("data connection closed.");
                                    break;
                                }


                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
            else{
                System.out.println("No such file exists ");

            }



        }


        System.out.println("closing socket");
        socket.close();


    }
}
