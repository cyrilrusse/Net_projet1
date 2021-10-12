package test;
import javafx.util.Pair;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class MHP {

    enum Type {SUBSCRIBE, PUBLISH, ACK, UNKNOWN, MALFORMED}

    static private byte[] build_header (int version, int type, int length) {
        return new byte[]{(byte)version, (byte)type, (byte)length};
    }

    static public void print_bits (byte[] msg) {
        for (byte b : msg) {
            System.out.println(Integer.toString(b,2));
        }
    }

    static public byte[] generate_subscribe (String identifier, String topic) throws AssertionError {
        /* Convert strings in utf-8*/
        byte[] identifierb = identifier.getBytes(StandardCharsets.UTF_8);
        byte[] topicb = topic.getBytes(StandardCharsets.UTF_8);
        int len = 3+2 + identifierb.length + topicb.length;
        if (2 + topicb.length + identifierb.length > 255)
            throw new AssertionError("Payload superior to 255 bytes");

        byte[] request = new byte[len];
        /* Build header */
        byte[] header = build_header(1,0,2 + identifierb.length + topicb.length);
        System.arraycopy(header,0, request,0, header.length);

        /* Build payload */
        request[header.length] = (byte) identifierb.length;
        System.arraycopy (identifierb, 0, request, header.length+1, identifierb.length);
        request[header.length+ identifierb.length+1] = (byte) topicb.length;
        System.arraycopy (topicb,0, request, header.length + identifierb.length + 2, topicb.length);

        return request;
    }

    static public byte[] generate_publish (String topic, String message) throws AssertionError{
        /* Convert strings in utf-8*/
        byte[] topicb = topic.getBytes(StandardCharsets.UTF_8);
        byte[] messageb = message.getBytes(StandardCharsets.UTF_8);
        int len = 3+2 + topicb.length + messageb.length;
        if (2 + topicb.length + messageb.length > 255)
            throw new AssertionError("Payload superior to 255 bytes");

        byte[] request = new byte[len];
        /* Build header */
        byte[] header = build_header(1,1,2 + messageb.length + topicb.length);
        System.arraycopy(header,0, request,0, header.length);

        /* Build payload */
        request[header.length] = (byte) topicb.length;
        System.arraycopy(topicb,0,request, header.length+1, topicb.length);
        request[header.length+ topicb.length+1] = (byte) messageb.length;
        System.arraycopy(messageb,0,request, header.length + topicb.length+2, messageb.length);

        return request;
    }

    static public byte[] generate_ack (String error) throws AssertionError{
        byte[] msgb;
        int nb_strings = 1;
        byte[] errorb = new byte[]{};
        if (error == null) {
            msgb = "OK".getBytes(StandardCharsets.UTF_8);
        } else {
            msgb = "ERROR".getBytes(StandardCharsets.UTF_8);
            errorb = error.getBytes(StandardCharsets.UTF_8);
            nb_strings = 2;
        }
        int len = 3+nb_strings + msgb.length+ errorb.length;
        if (nb_strings + msgb.length+ errorb.length > 255)
            throw new AssertionError("Payload superior to 255 bytes");

        byte[] request = new byte[len];

        /* Build header */
        byte[] header = build_header(1,2, nb_strings + msgb.length+ errorb.length);
        System.arraycopy(header,0, request,0, header.length);
        /* Build payload */
        request[header.length] = (byte) msgb.length;
        System.arraycopy(msgb,0, request, header.length+1, msgb.length);
        if (error != null) {
            request[header.length+ msgb.length+1] = (byte) errorb.length;
            System.arraycopy(errorb,0,request,header.length+ msgb.length+2, errorb.length);
        }
        return request;
    }

    /**
     * Reads a byte array and returns the type of MHP messages, as well as the application
     * data inside.
     */
    static public Pair<Type, Pair<String,String> > process_message (byte[] message) {
        if (message[0] != 1) // Check protocol version
            return new Pair<>(Type.MALFORMED, null);
        Type type = Type.PUBLISH;
        switch (message[1]) {
            case 0: // Type SUBSCRIBE
                type = Type.SUBSCRIBE;
            case 1: // Type PUBLISH
                /* Read first string */
                int topic_len = message[3];
                if (topic_len > message.length-3-1-2) // 3 for header, 2 for second string, which must at least be 2bytes long
                    return new Pair<>(Type.MALFORMED, null);
                String topic = new String(Arrays.copyOfRange(message, 4, 4+topic_len), StandardCharsets.UTF_8);

                /* Read second string */
                int app_msg_len = message[4+topic_len];
                if (app_msg_len >= message.length-3-(topic_len+1))
                    return new Pair<>(Type.MALFORMED, null);
                String application_msg = new String(Arrays.copyOfRange(message, 5+topic_len, 5+topic_len+app_msg_len), StandardCharsets.UTF_8);

                return new Pair<>(type, new Pair<>(topic, application_msg));
            case 2: // Type ACK
                /* Read first string */
                int err_len = message[3];
                if (err_len > message.length-3-1) // 3 for header
                    return new Pair<>(Type.MALFORMED, null);
                String err = new String(Arrays.copyOfRange(message, 4, 4+err_len), StandardCharsets.UTF_8);
                if (err.equals("OK"))
                    return new Pair<>(Type.ACK, new Pair<>(err, ""));

                /* Read second string */
                int err_msg_len = message[4+err_len];
                if (err_msg_len >= message.length-3-(err_len+1))
                    return new Pair<>(Type.MALFORMED, null);
                String err_msg = new String(Arrays.copyOfRange(message, 5+err_len, 5+err_len+err_msg_len), StandardCharsets.UTF_8);

                return new Pair<>(Type.ACK, new Pair<>(err, err_msg));
            default:
                return new Pair<>(Type.UNKNOWN, null); // Unexpected message type
        }
    }
}
