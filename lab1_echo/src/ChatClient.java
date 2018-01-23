import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by xian on 13/01/2016.
 */
public class ChatClient {
    public  static void main(String[] args)throws IOException {
        Socket socket=new Socket("localhost", 9876);
        BufferedReader reader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
        DataOutputStream writer=new DataOutputStream (socket.getOutputStream());


        //String result= reader.readLine();
        //System.out.println(result);
        BufferedReader buffer_in = new BufferedReader(new InputStreamReader(System.in));

        final boolean[] running = {true};

        Runnable reading = new Runnable() {
            @Override
            public void run() {
                try {
                    String reply;
                    while(running[0]) {
                        reply = reader.readLine();
                        if(!running[0]){
                            return;
                        }
                        System.out.println("Server reply: " + reply);

                        if(reply.equals("QUIT!!!")){
                            //buffer_in.close();
                            //writer.writeBytes("quit" + "\r\n");
                            running[0] = false;
                            //socket.close();
                            return;
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        Runnable writing = new Runnable() {
            @Override
            public void run() {
                try {
                    String userInput;
                    while(running[0]){
                        userInput = buffer_in.readLine();
                        if(!running[0]){
                            return;
                        }
                        writer.writeBytes(userInput + "\r\n");

                        if(userInput.equals("quit")){
                            writer.writeBytes("quit" + "\r\n");
                            running[0] = false;
                            break;
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        };
        ExecutorService executor = Executors.newCachedThreadPool();
        executor.submit(reading);
        executor.submit(writing);
        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            socket.close();
        }




    }

}
