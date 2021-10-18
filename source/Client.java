import java.net.Socket;
import java.io.OutputStream;
import java.io.IOException;


public class Client {
    public static void main(String[] argv)throws IOException{
        Socket s = new Socket("localhost", 2220);
        OutputStream out = s.getOutputStream();


        // Subscription to victory topic
        try{
            byte[] complete_msg = new MHP().createSubscriptionMsg("cyril", "victory");
            out.write(complete_msg);
            out.flush();
        }
        catch(SizeMessageError e){
            s.close();
            return;
        }
        catch (IOException e) {
            System.out.println("IO Exception\n");
        }

        // Read ACK and print
        Reader reader = new Reader(s);
        reader.read_message();
        String msg = reader.getMessage();
        System.out.println(msg);
        

        // Subscription to position topic
        try {
            byte[] complete_msg = new MHP().createSubscriptionMsg("cyril", "position");
            out.write(complete_msg);
            out.flush();
        }
        catch (SizeMessageError e){
            s.close();
            return;
        }
        catch (IOException e) {
            System.out.println("IO Exception\n");
        }

        // Read ACK and print
        reader = new Reader(s);
        reader.read_message();
        msg = reader.getMessage();
        System.out.println(msg);

        s.close();

        System.out.println("End of program.\n");
    }
}
