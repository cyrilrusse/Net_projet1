package test;
import javafx.util.Pair;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import static java.lang.System.exit;

public class Subscriber {

    private Socket socket;
    private OutputStream out;

    Reader reader;
    List<byte[]> messages;

    boolean gameIsOn = true;
    MHGame game;

    public Subscriber (String port) {
        try {
            /* Connect to Broker */
            socket = new Socket( "127.0.0.1", Integer.parseInt (port));
            socket.setTcpNoDelay(true);
            out = socket.getOutputStream();

            /* Setup reader */
            messages = new LinkedList<>();
            reader = new Reader(socket);
            reader.setup(false);

            /* Subscribe to victory and position topic */
            out.write (MHP.generate_subscribe("todo","victory"));
            out.flush();
            reader.read_msg(); // Discard ACK
            out.write (MHP.generate_subscribe("todo","position"));
            out.flush();
            reader.read_msg(); // Discard ACK
            reader.setup(true); // reader with timeout

            /* Setup MHGame */
            game = new MHGame(false);

        } catch (IOException e) {
            //e.printStackTrace();
            System.out.println("Error while connecting to Broker");
            exit (-1);
        }
    }

    /**
     * Reads all messages coming from Broker during 1 second
     * and updates the queue of messages.
     * @throws IOException if connection closed by peer.
     */
    private void read_messages () throws IOException {
        byte[] msg;
        while ((msg = reader.read_msg())!= null) {
            messages.add(msg);
        }
    }


    private void cleanup () {
        try {
            socket.close();
        } catch (IOException e) {
            System.out.println("Error while closing socket");
        }
    }

    public static void main (String[] argv) {
        if (argv.length != 1) {
            System.out.println("Error in the number of arguments");
            return;
        }
        System.out.println("Welcome to the Monster Hunting Game!");
        Subscriber subscriber = new Subscriber(argv[0]);
        Scanner scan = new Scanner(System.in);

        boolean first = true;
        while (subscriber.gameIsOn) {
            try {
                /* New iteration */
                subscriber.game.reset (); // Discard all measurements

                /* Read broker's messages */
                byte[] msg;
                while ((msg = subscriber.reader.read_msg()) != null) {
                    Pair<MHP.Type, Pair<String,String> > data = MHP.process_message(msg);
                    if (data.getKey() == MHP.Type.SUBSCRIBE) {
                        subscriber.out.write(MHP.generate_ack("Unexpected or Malformed packet"));
                        subscriber.out.flush();
                        continue;
                    }
                    if (data.getKey() == MHP.Type.PUBLISH) {
                        if (data.getValue().getKey().equals("victory")) {
                            System.out.println("/!\\ You got it /!\\"); //end of game
                            subscriber.cleanup();
                            exit (0);
                        }
                        if (data.getValue().getKey().equals("position")) {
                            subscriber.game.add_measure (data.getValue().getValue());
                        }
                        subscriber.out.write(MHP.generate_ack(null));
                        subscriber.out.flush();
                    }
                }

                if (first){
                    first = false;
                } else {
                    System.out.println("\nWhoops, you missed!");
                }


                subscriber.game.display ();
                //subscriber.messages.clear();

                /* User action and publish */
                System.out.println("\nTake a guess:");
                String input = scan.nextLine();
                subscriber.out.write (MHP.generate_publish("guess",input));
                subscriber.out.flush();

            } catch (IOException e) {
                System.out.println(e);
                break;
            }
        }
        subscriber.cleanup ();
        exit (0);
    }
}