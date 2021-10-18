import java.nio.charset.StandardCharsets;

import java.io.OutputStream;
import java.io.IOException;


public class Client {
    public static void main(String[] argv)throws IOException{
        String test_string = new String("D6");
        byte[] test = test_string.getBytes(StandardCharsets.UTF_8);
        byte test2 = 1;

        new Grid().reset();
        new Grid().intersection(test, test2);

        test_string = new String("E6");
        test2 = 2;
        test = test_string.getBytes(StandardCharsets.UTF_8);
        new Grid().intersection(test, test2);

        new Grid().display();
    }
}
