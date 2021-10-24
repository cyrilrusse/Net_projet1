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

    public boolean read_message(){
        try{
            this.s.setSoTimeout(1000);
            this.s.setTcpNoDelay(true);
            
            InputStream in = this.s.getInputStream();

            byte[] new_input = new byte[258];
            int len = in.read(new_input);

            System.arraycopy(new_input, 0, byte_received, nbr_byte, len);

            this.nbr_byte += len;
            
        }
        catch(SocketTimeoutException e){
            System.out.println("Time exceeded.\n");
            return false;
        }
        catch(IOException e){
            System.out.println("IO Excpetion.\n");
        }
        return true;
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

    public String getPositionMessage(){
        byte[] msg_to_decode = new byte[byte_received[this.position_message+2]+3];
        System.arraycopy(byte_received, position_message, msg_to_decode, 0, 
                byte_received[this.position_message + 2] + 3);      
        if(msg_to_decode[0]!=1)
            return null;
        if(msg_to_decode[1]!=1)
            return null;


        int taille_first_word = msg_to_decode[3];
        byte[] first_word = new byte[taille_first_word];

        //(source, pos_source, dest, pos_dest, len)
        System.arraycopy(msg_to_decode, 4, first_word, 0, taille_first_word);

        String test = new String(first_word, StandardCharsets.UTF_8);

        if(!(test.equals("position")))
            System.out.print("Not a message position.\n");

        int position_second_word = taille_first_word+5;
        int taile_second_word = msg_to_decode[taille_first_word+4];
        byte[] second_word = new byte[taile_second_word];
        System.arraycopy(msg_to_decode, position_second_word, second_word, 0, taile_second_word);

        this.computeNextMessageIndex();

        return new String(second_word, StandardCharsets.UTF_8);        
    }

    public Message decodeMessage(){
        String[] words = new String[2];
        int payload_length = byte_received[position_message+2];
        int total_length = payload_length +3;
        byte[] msg_to_decode = new byte[total_length];
        int i = 3, number_word = 0, word_length;
        byte[] word_byte;



        while(i<total_length){
            word_length = msg_to_decode[i++];
            word_byte = new byte[word_length];
            System.arraycopy(msg_to_decode, i, word_byte, 0, word_length);
            words[number_word++] = new String(word_byte, StandardCharsets.UTF_8);
            i+=word_length;
        }
        if(i>total_length || number_word!=2)
            return null;
        
        return new Message(msg_to_decode[1], words, number_word);
    }

    public void computeNextMessageIndex(){
        this.position_message += this.byte_received[position_message+2]+3;
    }

}
