import java.nio.charset.StandardCharsets;

public class MHP {
    

    public byte[] createSubscriptionMsg(String client_name, String sub_topic)throws SizeMessageError{
        byte name_length = (byte) client_name.length();
        byte sub_topic_length = (byte) sub_topic.length();
        byte total_length = (byte) (name_length + sub_topic_length + 2);//word_length + words
        
        //header array for subscription
        byte[] header = new byte[3];
        header[0] = 1;// version 1
        header[1] = 0;// type 0 (subscribe)
        header[2] = (byte) total_length;

        if((total_length+3)>255)throw new SizeMessageError("Maximum message size exceeded");

        // String client_name = new String("cyril");
        // String sub_topic = new String("victory");
        


        byte[] msg = new byte[total_length];

        msg[0] = name_length;

        byte[] client_name_encode_utf8 = client_name.getBytes(StandardCharsets.UTF_8);

        System.arraycopy(client_name_encode_utf8, 0, msg, 1, name_length);

        msg[name_length + 1] = sub_topic_length;
        byte[] sub_topic_encode_utf8 = sub_topic.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(sub_topic_encode_utf8, 0, msg, name_length + 2, sub_topic_length);

        byte[] complete_msg = new byte[total_length + 3];// msg+header
        System.arraycopy(header, 0, complete_msg, 0, 3);
        System.arraycopy(msg, 0, complete_msg, 3, total_length);

        return complete_msg;
    }

}

class SizeMessageError extends Exception{
    public SizeMessageError()           {super();}
    public SizeMessageError(String s)   {super(s);}
}
