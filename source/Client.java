import java.net.Socket;
import java.net.SocketException;
import java.io.OutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class Client {
    public static void main(String[] argv){
        try{
            Socket s = new Socket("localhost", 2220);
            
            OutputStream out = s.getOutputStream();
            InputStream in = s.getInputStream();
            byte[] msg = new byte[17];
            s.setTcpNoDelay(true);
            s.setSoTimeout(1000);
            
            
            msg[0] = 1;//version 1
            msg[1] = 0;//type 0 (subscribe)
            msg[2] = 15;//length 15
            
            msg[3] = 5;//length 5 : "cyril"
            String client_name = new String("cyril");
            byte[] client_name_encode_utf8 = client_name.getBytes(StandardCharsets.UTF_8);
            for(int i = 4; i<9; i++)
                msg[i] = client_name_encode_utf8[i-4];
            // msg[4] =
            // msg[5] =
            // msg[6] =
            // msg[7] =
            // msg[8] =
            msg[9] = 7;//length 7 : "victory"
            String sub_topic = new String("victory");
            byte[] sub_topic_encode_utf8 = sub_topic.getBytes(StandardCharsets.UTF_8);
            for(int i = 10; i<17; i++)
                msg[i] = sub_topic_encode_utf8[i-10];
            
            
            out.write(msg);
            out.flush();
            
            byte[] received_msg = new byte[64];
            while(true){
                int len = in.read(received_msg);
                if(len<=0)
                    break;
                System.out.println(len);
                for(int i = 0; i<len; i++)
                    System.out.println(received_msg[i]);
            }

            s.close();
        }
        catch(SocketException e){
            System.out.println("time exceded\n");
        }
        catch(IOException e){
            System.out.println("IO problém\n");
        }
    }
}
