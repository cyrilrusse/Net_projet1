import java.net.Socket;
import java.io.OutputStream;
import java.io.IOException;
import java.util.*;


public class Client {
    public static void main(String[] argv)throws IOException{
        Socket s = new Socket("localhost", 2220);
        OutputStream out = s.getOutputStream();
        Scanner guess_entry = new Scanner(System.in);


        // Subscription to victory topic
        try{
            byte[] subscription_msg = new MHP().createSubscriptionMsg("cyril", "victory");
            if(subscription_msg==null){
                s.close();
                return;
            }
            out.write(subscription_msg);
            out.flush();
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
            byte[] subscription_msg = new MHP().createSubscriptionMsg("cyril", "position");
            if (subscription_msg == null) {
                s.close();
                return;
            }
            out.write(subscription_msg);
            out.flush();
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
                out.write(new MHP().createAckMsg(true));
                out.flush();
                grid.intersectionString(position);
            }
        }
        grid.display();

        System.out.print("Take a guess: ");
        String input = guess_entry.nextLine();

        try {
            byte[] guess_msg = new MHP().createGuessMsg(input);
            if (guess_msg == null) {
                s.close();
                return;
            }
            out.write(guess_msg);
            out.flush();
        } catch (IOException e) {
            System.out.println("IO Exception\n");
        }

        reader = new Reader(s);
        reader.read_message();
        msg = reader.getMessage();
        System.out.println(msg);

        reader = new Reader(s);
        reader.read_message();
        msg = reader.getMessage();
        System.out.println(msg);

        
        

        s.close();

        System.out.println("End of program.\n");
    }
}
