import java.net.Socket;
import java.nio.charset.StandardCharsets;
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





        Grid grid = new Grid();
        grid.reset();
        boolean read = true;
        String position = "";
        while(true){
            reader = new Reader(s);
            read = reader.read_message();
            if(!read)
                break;
            while(reader.messageToDecodeRemaining()){
                position = reader.getPositionMessage();
                if(position==null)
                    break;
                grid.intersectionString(position);
            }
        }
        grid.display();


        s.close();

        System.out.println("End of program.\n");
    }
}
