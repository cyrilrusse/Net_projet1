import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeoutException;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;


public class Client {
    public static void main(String[] argv)throws IOException{
        try{
            Socket s = new Socket("localhost", 2220);
            s.setSoTimeout(1000);
            s.setTcpNoDelay(true);
            OutputStream out = s.getOutputStream();
            InputStream in = s.getInputStream();

            try{
                byte[] complete_msg = new MHP().create_subscription_msg("cyril", "victory");
                out.write(complete_msg);
                out.flush();
            }
            catch(SizeMessageError e){
                s.close();
                return;
            }

            
            byte[] received_msg = new byte[64];
            while (true) {
                int len = in.read(received_msg);
                if (len <= 0)
                    break;
                for (int i = 0; i < len; i++)
                    System.out.println(received_msg[i]);
                if (len == 6)
                    break;
            }
            

            try {
                byte[] complete_msg = new MHP().create_subscription_msg("cyril", "position");
                out.write(complete_msg);
                out.flush();
            }
            catch (SizeMessageError e) {
                s.close();
                return;
            }

            while (true) {
                int len = in.read(received_msg);
                if (len <= 0)
                    break;
                for (int i = 0; i < len; i++)
                    System.out.println(received_msg[i]);
                if(len==6)
                    break;
            }

            s.close();
        }
        catch (SocketTimeoutException e){
            System.out.println("no more message to receive.\n");
        }
        catch(IOException e){
            System.out.println("IO Exception\n");
        }

        System.out.println("program keep going\n");
    }
}
