import java.net.Socket;
import java.net.UnknownHostException;
import java.io.OutputStream;
import java.io.IOException;
import java.util.*;


public class Client {

    public static void clean(Socket s, Scanner guess_entry)throws IOException{
        s.close();
        guess_entry.close();
    }
    

    public static void main(String[] argv)throws IOException{
        if(argv.length!=1){
            System.out.println("\nThis program requiere exaclty one arguement : the port for the connexion.\njava Client <port>\n");
            return;
        }
        int port = Integer.parseInt(argv[0]);

        Socket s;
        Scanner guess_entry = new Scanner(System.in);
        Reader reader;
        byte[] subscription_msg;
        String input;
        
        try{
            s = new Socket("localhost", port);
        }
        catch(UnknownHostException e){
            System.out.println("Error : Unknown host.");
            guess_entry.close();
            return;
        }
        OutputStream out = s.getOutputStream();
        
        
        // Subscription to victory topic
        try{
            subscription_msg = new MHP().createSubscriptionMsg("cyril", "victory");
            if(subscription_msg==null){
                s.close();
                guess_entry.close();
                return;
            }
            out.write(subscription_msg);
            out.flush();
        }
        catch (IOException e) {
            System.out.println("Error while sending subscription message.");
        }
        
        // Read ACK and print
        reader = new Reader(s);
        reader.read_message();
        String msg = reader.getMessage();
        System.out.println(msg);
        

        // Subscription to position topic
        try {
            subscription_msg = new MHP().createSubscriptionMsg("cyril", "position");
            if (subscription_msg == null) {
                guess_entry.close();
                s.close();
                return;
            }
            out.write(subscription_msg);
            out.flush();
        }
        catch (IOException e) {
            System.out.println("Error while sending subscription message.");
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

        while(true){
            System.out.print("Make a guess: ");
            input = guess_entry.nextLine();
            try {
                byte[] guess_msg = new MHP().createGuessMsg(input);
                out.write(guess_msg);
                out.flush();
            }
            catch(MessageException e){
                System.out.println("Please ensure to make your guess following the same format as the following example : C7.");
                continue;
            }
            catch (IOException e) {
                System.out.println("Error while sending guess message.");
                clean(s, guess_entry);
                return;
            }
            break;
        }

        reader = new Reader(s);
        reader.read_message();
        msg = reader.getMessage();
        System.out.println(msg);

        reader = new Reader(s);
        reader.read_message();
        msg = reader.getMessage();
        System.out.println(msg);


        guess_entry.close();
        s.close();

        System.out.println("End of program.\n");
    }
}
