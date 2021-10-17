import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.io.InputStream;

public class Reader {
    Socket s;
    byte[] byte_received = new byte[512];
    int nbr_byte = 0;

    public Reader(Socket socket){
        this.s = socket;
    }


    public void read_message(){
        try{
            this.s.setSoTimeout(1000);
            this.s.setTcpNoDelay(true);
            
            InputStream in = this.s.getInputStream();

            byte[] new_input = new byte[256];
            int len = in.read(new_input);

            System.arraycopy(new_input, 0, byte_received, nbr_byte, len);

            this.nbr_byte += len;
            

        }
        catch(SocketTimeoutException e){
            System.out.println("Time exceeded.\n");
        }
        catch(IOException e){
            System.out.println("IO Excpetion.\n");
        }

    }

    public int getTypeMessage(){
        if(nbr_byte==0)
            return -1;
        return byte_received[1];
    }

    public String getMessage(){
        byte total_length = byte_received[2];
        byte[] word;
        String msg = "";
        int i = 0;
        byte size_word;

        while(i<total_length){
            size_word = byte_received[i+3];
            if((i+size_word)>total_length)
                return null;//should be an Exception
            word = new byte[size_word];
            System.arraycopy(byte_received, i+4, word, 0, size_word);
            msg += new String(word, StandardCharsets.UTF_8);
            i+=size_word+1;
        }


        return msg;
    }

    // public boolean ackMessage(){}



}
