import java.nio.charset.StandardCharsets;

public class MHP {
    
    public byte[] createGuessMsg(String guess)throws MessageException{
        int length = guess.length();
        if(length>3 || length<2 || guess.charAt(0)<'A' || guess.charAt(0)>'J')
            throw new MessageException("Wrong format for your guess.");
        if(guess.charAt(1)<'1'||guess.charAt(1)>'9')
            throw new MessageException("Wrong format for your guess.");
        if(length==3 && (guess.charAt(1)!='1' || guess.charAt(2)!='0'))
            throw new MessageException("Wrong format for your guess.");

        try{
            return createMsg("guess", guess, (byte)1);
        }
        catch (SizeMessageError e) {
            return null;
        }
    }

    public byte[] createSubscriptionMsg(String client_name, String sub_topic){
        try{
            return createMsg(client_name, sub_topic, (byte)0);
        }
        catch (SizeMessageError e) {
            return null;
        }
    }

    public byte[] createAckMsg(boolean ok){
        String ack_string;
        if(ok)
            ack_string = new String("OK");
        else
            ack_string = new String("ERROR");

        try{
            return createMsg(ack_string, null, (byte)2);
        }
        catch(SizeMessageError e){
            return null;
        }

    }

    static byte[] createMsg(String word1, String word2, byte type)throws SizeMessageError{
        byte word1_length = (byte) word1.length();
        byte payload_length;
        if(word2!=null){
            byte word2_length = (byte) word2.length();
            payload_length = (byte) (word1_length + word2_length + 2);// words_length + words
        }
        else
            payload_length = (byte) (word1_length + 1);

        if ((payload_length + 3) > 258)
            throw new SizeMessageError("Maximum message size exceeded");

        // header array for subscription
        byte[] header = createHeader(type, payload_length);

        // payload
        byte[] payload = createPayload(word1, word2, payload_length);

        // concatenate the payload and the header
        byte[] complete_msg = new byte[payload_length + 3];
        System.arraycopy(header, 0, complete_msg, 0, 3);
        System.arraycopy(payload, 0, complete_msg, 3, payload_length);

        return complete_msg;
    }

    static byte[] createHeader(byte type_msg, byte payload_length){
        return new byte[]{(byte)1, type_msg, payload_length};
    }

    static byte[] createPayload(String word1, String word2, byte payload_length){
        byte[] payload = new byte[(int)payload_length];
        byte word1_length = (byte)word1.length();
        
        
        payload[0] = word1_length;
        
        byte[] word1_encode_utf8 = word1.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(word1_encode_utf8, 0, payload, 1, word1_length);
        
        if(word2==null)
            return payload;

        byte word2_length = (byte)word2.length();

        payload[word1_length + 1] = word2_length;

        byte[] word2_encode_utf8 = word2.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(word2_encode_utf8, 0, payload, word1_length + 2, word2_length);

        return payload;
    }

}

class SizeMessageError extends Exception{
    public SizeMessageError()           {super();}
    public SizeMessageError(String s)   {super(s);}
}
