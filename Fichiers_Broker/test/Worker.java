package test;
import javafx.util.Pair;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Worker implements Runnable {
    Socket s;
    Reader reader;
    OutputStream out;

    /* State */
    Defaultdict<String, List<Pair<String,OutputStream>>> topic_to_subscriber;
    MHGame game;

    Worker(Socket s, boolean cheat) {
        this.s = s;
        this.topic_to_subscriber = new Defaultdict<>(ArrayList.class);
        this.game = new MHGame(cheat);
        this.game.launch_game();
    }

    @Override
    public void run() {
        try {
            s.setTcpNoDelay(true);
            reader = new Reader(s);
            reader.setup(false);
            out = s.getOutputStream();

            byte[] msg;
            while (true) {
                msg =  reader.read_msg();
                /* Process message and answer */
                Pair<MHP.Type, Pair<String,String>> data = MHP.process_message(msg);
                System.out.print("Message received: ");

                switch (data.getKey()) {
                    case SUBSCRIBE:
                        System.out.println("SUBSCRIBE");
                        /* Register client */
                        topic_to_subscriber.get(data.getValue().getValue()).add(new Pair<>(data.getValue().getKey(), out));
                        out.write(MHP.generate_ack(null));
                        /* Play the game */
                        if (data.getValue().getValue().equals("position")) {
                            List<String> positions = game.next_iteration (); // Send back first round of messages
                            for (String pos: positions) {
                                forward_messages("position", pos);
                            }
                        }
                        break;
                    case PUBLISH:
                        System.out.println("PUBLISH");
                        out.write(MHP.generate_ack(null));
                        //forward_messages(data.getValue().getKey(), data.getValue().getValue()); // Not actually used right now, because sensors are simulated.
                        process_publish (data.getValue()); // For Message destined to server (guess topic)
                        break;
                    case MALFORMED:
                        System.out.println("MALFORMED MSG");
                        out.write(MHP.generate_ack("Malformed packet"));
                        break;
                    case UNKNOWN:
                        System.out.println("UNKNOWN MSG");
                        out.write(MHP.generate_ack("Unknown Message Type"));
                        break;
                    case ACK:
                        System.out.println("ACK");
                        break;
                    default:
                }
                out.flush();
            }

        } catch (IOException e) {
            System.out.println("Client disconnected");
        }
    }

    /**
     * Processes a message destined to the server (guess topic)
     * topic - msg
     */
    private void process_publish (Pair<String,String> data) throws IOException {
        if (data.getKey().equals("guess")) {
            boolean victory;
            try {
                victory = game.submit_guess (data.getValue());
            } catch (IOException e) {
                out.write(MHP.generate_ack(e.getMessage()));
                out.flush();
                return;
            }

            /* victory */
            if (victory) {
                out.write (MHP.generate_publish("victory", "victory!"));
                out.flush();
            } else { /* next iteration */
                List<String> positions = game.next_iteration (); // Send back first round of messages
                for (String pos: positions) {
                    forward_messages("position", pos);
                }
            }
        }
    }

    /**
     * Given a message in a topic, forwards it to every subscribed client.
     */
    private void forward_messages (String topic, String data) throws IOException {
        List<Pair<String,OutputStream>> subscribers = topic_to_subscriber.get(topic);

        for (Pair<String,OutputStream> subscriber: subscribers) {
            subscriber.getValue().write(MHP.generate_publish (topic, data));
            subscriber.getValue().flush();
            //reader.read_msg(); // Discard ACK.
        }
    }
}
