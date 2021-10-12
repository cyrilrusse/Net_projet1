package test;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class Reader {
    Socket s;
    InputStream in;

    static final int MSG_MAXSIZE = 258;
    byte[] stream = new byte[4096]; // Used to store remaining data in between packets read.
    int nb_bytes = 0;

    Reader (Socket _s) {s = _s;}

    /**
     * Set the timeout on the socket (if asked) and get the input stream.
     * @throws IOException if failure to get input stream
     */
    public void setup (boolean timeout) throws IOException {
        if (timeout)
            s.setSoTimeout(1000);
        in = s.getInputStream();
    }

    /**
     * Packet oriented communication using a stream protocol such as TCP.
     *      * @return a complete Application Message received on the TCP socket.
     *      * If no Application Message has been received in a given amount of time,
     *      * return null.
     * @return a complete Application Message received on the TCP socket, null
     *          if no Application Message has been received after timeout.
     * @throws IOException if connection closed by peer.
     */
    public byte[] read_msg () throws IOException {
        byte[] msg = new byte[MSG_MAXSIZE]; // Tmp buffer to put read data in.
        int msg_length = 0; // The length of the current message we are reading.
        while (true) {
            if (nb_bytes >= 3 && msg_length == 0) { // Got the header, parse it.
                msg_length = stream[2];
            }
            if (nb_bytes >= msg_length + 3) {
                byte[] r = new byte[3+msg_length];
                System.arraycopy(stream, 0, r, 0, 3+msg_length);
                System.arraycopy(stream, 3+msg_length, stream, 0, nb_bytes-(3+msg_length));
                nb_bytes -= (3+msg_length);
                return r;
            }

            int len;
            try {
                len = in.read(msg);
            } catch (SocketTimeoutException e) {
                return null;
            }
            if (len < 0) throw new IOException("Connection may have been closed by peer"); // Connection closed by peer

            System.arraycopy(msg, 0, stream, nb_bytes, len); // Add to global stream.
            nb_bytes += len;
        }
    }
}
