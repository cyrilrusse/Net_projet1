import java.net.Socket;
import java.net.UnknownHostException;
import java.io.OutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class Subscriber {

    private static void clean(Socket s, Scanner guess_entry, InputStream in, OutputStream out){
        try{
            in.close();
            out.close();
            guess_entry.close();
            s.close();
        }
        catch(IOException e){
            System.out.println("Error occured when closing socket.");
        }
    }
    
    private static void sendSub(OutputStream out, String sub_topic)throws IOException, MHPException{
        byte[] subscription_msg;
        subscription_msg = new MHP().createSubscriptionMsg("cyril", sub_topic);
        out.write(subscription_msg);
        out.flush();
    }

    private static boolean sendAck(OutputStream out, boolean ack){
        try {
            out.write(new MHP().createAckMsg(ack));
            out.flush();
        } catch (IOException e) {
            System.out.println("Error while sending ACK message.");
            return false;
        } catch (MHPException e) {
            System.out.println("Couldn't create ACK message.");
            return false;
        }
        return true;
    }

    private static boolean requestGuess(OutputStream out, Scanner guess_entry){
        while (true) {
            System.out.print("\nMake a guess: ");
            String input = guess_entry.nextLine();
            try {
                byte[] guess_msg = new MHP().createGuessMsg(input);
                out.write(guess_msg);
                out.flush();
            } catch (MHPException e) {
                System.out.println(
                        "\nPlease ensure to make your guess following the same format as the following example : C7.\n");
                continue;
            } catch (IOException e) {
                System.out.println("Error while sending guess message.");
                return false;
            }
            break;
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
        Boolean in_game = true; 
        InputStream in;
        OutputStream out;
        Message message_decoded;
        
        // Connection
        try{
            s = new Socket("localhost", port);
            out = s.getOutputStream();
            in = s.getInputStream();
        }catch(UnknownHostException e){
            System.out.println("Error : Unknown host.");
            guess_entry.close();
            return;
        }catch(IOException e){
            System.out.println("Error while creating socket.");
            guess_entry.close();
            return;
        }
        
        
        // Subscription to victory topic
        try {
            sendSub(out, "victory");
        } catch (Exception e) {
            System.out.println("Error while sending subscription message.");
            clean(s, guess_entry, in, out);
            return;
        }
        
        // Read Subscription ACK
        reader = new Reader(s);
        try{
            if(!reader.read_message(in)){
                System.out.println("Connection might have been closed by the broker.");
                clean(s, guess_entry, in, out);
                return;
            }
        }catch(IOException e){
            System.out.println("Error with inputstream reader.");
            clean(s, guess_entry, in, out);
            return;
        }
        
        //check ACK
        try{
            message_decoded = reader.decodeMessage();
            if(!new MHP().checkAck(message_decoded)){
                clean(s, guess_entry, in, out);
                System.out.println("Error received while subscribing to victory topic.");
                if (message_decoded.getNbrOfWords() == 2)
                    System.out.println(message_decoded.words[1]);
                return;
            }
        } catch (MHPException e) {
            clean(s, guess_entry, in, out);
            System.out.println("Wrong format of ack received.");
            return;
        }

        // Subscription to position topic
        try{
            sendSub(out, "position");
        } catch (Exception e) {
            System.out.println("Error while sending subscription message.");
            clean(s, guess_entry, in, out);
            return;
        }

        // Read Subscription ACK
        reader = new Reader(s);
        try{
            if(!reader.read_message(in)){
                System.out.println("Connection might have been closed by the broker.");
                clean(s, guess_entry, in, out);
                return;
            }
        }catch(IOException e){
            System.out.println("Error with inputstream reader.");
            clean(s, guess_entry, in, out);
            return;
        }
        
        // check ACK
        try {
            message_decoded = reader.decodeMessage();
            if (!new MHP().checkAck(message_decoded)) {
                clean(s, guess_entry, in, out);
                System.out.println("Error received while subscribing to victory topic:");
                if(message_decoded.getNbrOfWords()==2)
                    System.out.println(message_decoded.words[1]);
                return;
            }
        }catch (MHPException e) {
            clean(s, guess_entry, in, out);
            System.out.println("Wrong format of ack received.");
            return;
        }

        // Game
        reader = new Reader(s);
        Grid grid = new Grid();
        String position;
        boolean read = false;
        message_decoded = null;

        System.out.println("\nWelcome to the monster hunting game!\n");

        while(in_game){
            grid.reset();
            // Read all positions sent
            while(!read){
                try{
                    read = reader.read_message(in);
                }
                catch(IOException e){
                    System.out.println("Error with inputstream reader.");
                    clean(s, guess_entry, in, out);
                    return;
                }
            }

            // Decode all position received
            while(reader.messageToDecodeRemaining() || message_decoded!=null){
                try{
                    if(message_decoded==null)
                        message_decoded = reader.decodeMessage();
                    position = reader.getPositionMessage(message_decoded);
                }catch(MHPException e){
                    clean(s, guess_entry, in, out);
                    sendAck(out, false);
                    System.out.println("Error while trying to decode an expected position message.");
                    return;
                }

                // Send positive ACK
                if(!sendAck(out, true)){
                    clean(s, guess_entry, in, out);
                    return;
                }

                // Make the intersection with current sensors on the grid
                grid.intersectionString(position);
                message_decoded = null;
            }
            grid.display();

            // Asking for a guess

            if(!requestGuess(out, guess_entry)){
                clean(s, guess_entry, in, out);
                return;
            }

            // Read guess ACK 
            reader = new Reader(s);
            try{
                reader.read_message(in);
            }catch (IOException e) {
                System.out.println("Error with inputstream reader.");
                clean(s, guess_entry, in, out);
                return;
            }

            // Check ACK
            try {
                message_decoded = reader.decodeMessage();
                if (!new MHP().checkAck(message_decoded)) {
                    clean(s, guess_entry, in, out);
                    System.out.println("Error ACK received while publishing guess.");
                    return;
                }
            }catch (MHPException e) {
                clean(s, guess_entry, in, out);
                System.out.println("Message received was expected to be an ACK message.");
                return;
            }

            // Read potential victory message
            reader = new Reader(s);
            try{
                reader.read_message(in);
            }catch (IOException e) {
                System.out.println("Error with inputstream reader.");
                clean(s, guess_entry, in, out);
                return;
            }

            // Check if victory message received and win if so
            try{
                message_decoded = reader.decodeMessage();
            }
            catch(MHPException e){
                clean(s, guess_entry, in, out);
                System.out.println("Unexpected format received.");
                return;
            }
            if(message_decoded.getWordAt(0).equals("victory") && message_decoded.getWordAt(1).equals("victory!")){
                System.out.println("\nYou got it!\n");
                in_game = false;
            }
            else{
                System.out.println("\noops, try again...\n");
                read = false;
            }
        }

        clean(s, guess_entry, in, out);
    }
}
