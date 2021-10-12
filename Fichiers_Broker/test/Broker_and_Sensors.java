package test;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.System.exit;

public class Broker_and_Sensors {

    static int port;
    static boolean cheat = false;

    public static void main (String[] argv) {
        if (argv.length < 1){
            System.out.println("Wrong number of arguments.\n Use 'java Broker_and_Sensors <port> [cheat]'");
            exit (-1);
        }
        try {
            port = Integer.parseInt(argv[0]);
        } catch (NumberFormatException e) {
            System.out.println("Error in port number: " + e.getMessage());
            exit (-1);
        }

        if (argv.length == 2 && argv[1].equals("cheat")) {
            cheat = true;
        }

        /* Create thread pool and Server socket */
        ExecutorService threadPool = Executors.newFixedThreadPool(10);
        ServerSocket ss = null;
        try {
            ss = new ServerSocket(port);
        } catch(IOException e) {
            System.out.println("Cannot instantiate server socket, exiting...");
            exit(1);
        }
        System.out.println("Server ready");

        /* Accept connections and spawn threads */
        while(true)
        {
            try
            {
                Socket sClient = ss.accept();
                System.out.println("Connection client");
                threadPool.submit(new Worker(sClient, cheat));
            }
            catch(IOException e)
            {
                System.out.println("Input/Output error with client socket");
            }
        }
    }
}
