import java.net.Socket;
import java.net.SocketTimeoutException;

import java.nio.charset.StandardCharsets;

import java.io.IOException;
import java.io.InputStream;

public class Reader {
    Socket s;
    byte[] byte_received = new byte[1024];
    int nbr_byte = 0;
    int position_message = 0;

    public Reader(Socket socket){
        this.s = socket;
    }

    public boolean messageToDecodeRemaining(){
        if(nbr_byte>position_message)
            return true;
        return false;
    }

    public boolean read_message(InputStream in)throws IOException{
        this.s.setSoTimeout(1000);
        this.s.setTcpNoDelay(true);
        
        byte[] new_input = new byte[258];
        int len = 0;
        try{
            len = in.read(new_input);
        }catch(SocketTimeoutException e){
            return false;
        }

        System.arraycopy(new_input, 0, byte_received, nbr_byte, len);

        this.nbr_byte += len;

        return true;
    }

    public int getTypeMessage(){
        if(nbr_byte==0)
            return -1;
        return byte_received[1];
    }

    public String getPositionMessage(Message msg_to_decode)throws MHPException{     
        if(msg_to_decode.getType()!=1)
            throw new MHPException("Unexpected message. Publish message expected.");
        if(!msg_to_decode.words[0].equals("position"))
            throw new MHPException("Expected a position message but"+msg_to_decode.words[0]+"message given.");

        return msg_to_decode.words[1];   
    }

    public Message decodeMessage()throws MHPException{
        String[] words = new String[2];
        int payload_length = byte_received[position_message+2];
        int total_length = payload_length +3;
        byte[] msg_to_decode = new byte[total_length];
        int i = 3, number_word = 0, word_length;
        byte[] word_byte;

        System.arraycopy(byte_received, position_message, msg_to_decode, 0, total_length);
        
        while(number_word<2 && i<total_length){
            word_length = msg_to_decode[i++];
            word_byte = new byte[word_length];
            System.arraycopy(msg_to_decode, i, word_byte, 0, word_length);
            words[number_word++] = new String(word_byte, StandardCharsets.UTF_8);
            i+=word_length;
        }
        if(i>total_length || number_word>2)
            throw new MHPException("Unexpected format received.");

        computeNextMessageIndex();

        return new Message(msg_to_decode[1], words, number_word);
    }

    public void computeNextMessageIndex(){
        this.position_message += this.byte_received[position_message+2]+3;
    }

}
