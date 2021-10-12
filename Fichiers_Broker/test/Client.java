package test;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Client {
    public static void main (String[] argv) throws Exception {
        Socket s = new Socket("address of server", 8086); // Connect to server
        OutputStream out = s.getOutputStream();
        InputStream in = s.getInputStream();
        byte[] msg = new byte[64];
        while (true) {
            int len = in.read(msg);
            if (len <= 0) break; // Connection may have been closed by the other side
            out.write((len + " bytes received").getBytes());
            out.flush();
        }
        s.close();
    }
}
