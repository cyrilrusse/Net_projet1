import java.net.Socket;
import java.net.UnknownHostException;
import java.io.OutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class Client {

    public static void clean(Socket s, Scanner guess_entry){
        try{
            guess_entry.close();
            s.close();
        }
        catch(IOException e){
            System.out.println("Error occured when closing socket.");
        }
    }
    
    public static boolean sendSub(OutputStream out, String sub_topic){
        byte[] subscription_msg;
        try {
            subscription_msg = new MHP().createSubscriptionMsg("cyril", sub_topic);
            out.write(subscription_msg);
            out.flush();
        } catch (IOException e) {
            System.out.println("Error while sending subscription message.");
            return false;
        }
        return true;
    }

    public static void main(String[] argv){
        if(argv.length!=1){
            System.out.println("\nThis program requiere exaclty one arguement : the port for the connexion.\njava Client <port>\n");
            return;
        }
        int port = Integer.parseInt(argv[0]);

        Socket s;
        Scanner guess_entry = new Scanner(System.in);
        Reader reader;
        String input;
        Boolean in_game = true;
        InputStream in;
        OutputStream out;
        
        
        try{
            s = new Socket("localhost", port);
            out = s.getOutputStream();
            in = s.getInputStream();
        }
        catch(UnknownHostException e){
            System.out.println("Error : Unknown host.");
            guess_entry.close();
            return;
        }
        catch(IOException e){
            System.out.println("Error while creating socket.");
            guess_entry.close();
            return;
        }
        
        
        // Subscription to victory topic
        if(!sendSub(out, "victory")){
            clean(s, guess_entry);
            return;
        }
        
        // Read ACK
        reader = new Reader(s);
        reader.read_message(in);
        Message message_decoded;
        try{
            message_decoded = reader.decodeMessage();
            if(!new MHP().checkAck(message_decoded)){
                clean(s, guess_entry);
                System.out.println("Error received while subscribing to victory topic.");
                return;
            }
        }
        catch(MessageException e){
            clean(s, guess_entry);
            System.out.println("Unexpected format received.");
            return;
        }
        catch(MHPException e){
            clean(s, guess_entry);
            System.out.println("Wrong format of ack received.");
            return;
        }

        // Subscription to position topic
        if(!sendSub(out, "position")){
            clean(s, guess_entry);
            return;
        }

        // Read ACK
        reader = new Reader(s);
        reader.read_message(in);
        try {
            message_decoded = reader.decodeMessage();
            if (!new MHP().checkAck(message_decoded)) {
                clean(s, guess_entry);
                System.out.println("Error received while subscribing to victory topic.");
                return;
            }
        } catch (MessageException e) {
            clean(s, guess_entry);
            System.out.println("Unexpected format received.");
            return;
        } catch (MHPException e) {
            clean(s, guess_entry);
            System.out.println("Wrong format of ack received.");
            return;
        }

        while(in_game){
            Grid grid = new Grid();
            grid.reset();
            boolean read = true;
            String position = "";
            reader = new Reader(s);
            while(true){
                read = reader.read_message(in);
                if(!read)
                    break;
                while(reader.messageToDecodeRemaining()){
                    position = reader.getPositionMessage();
                    if(position==null)
                        break;
                    try{
                    out.write(new MHP().createAckMsg(true));
                    out.flush();
                    }
                    catch(IOException e){
                        clean(s, guess_entry);
                        return;
                    }
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

            // Read ACK
            reader = new Reader(s);
            reader.read_message(in);
            try {
                message_decoded = reader.decodeMessage();
                if (!new MHP().checkAck(message_decoded)) {
                    clean(s, guess_entry);
                    System.out.println("Error received while subscribing to victory topic.");
                    return;
                }
            } catch (MessageException e) {
                clean(s, guess_entry);
                System.out.println("Unexpected format received.");
                return;
            } catch (MHPException e) {
                clean(s, guess_entry);
                System.out.println("Wrong format of ack received.");
                return;
            }

            //
            reader = new Reader(s);
            reader.read_message(in);
            try{
                message_decoded = reader.decodeMessage();
            }
            catch(MessageException e){
                clean(s, guess_entry);
                System.out.println("Unexpected format received.");
                return;
            }
            if(message_decoded.getWordAt(0).equals("victory") && message_decoded.getWordAt(1).equals("victory!")){
                in_game = false;
                System.out.println("You got it!");
            }
            else{
                System.out.println("oops, try again...");
            }
        }

        clean(s, guess_entry);
    }
}
