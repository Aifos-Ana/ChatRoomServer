import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

public class TimeServer {
    private static final int THREAD_POOL_SIZE = 20;

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java TimeServer <port>");
            return;
        }

        int port = Integer.parseInt(args[0]);
        RoomManager roomManager = new RoomManager();
        ExecutorService pool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        // Load keystore for SSL
        System.setProperty("javax.net.ssl.keyStore", "keystore.jks");
        System.setProperty("javax.net.ssl.keyStorePassword", "cpd2425");

        try{
            SSLServerSocketFactory ssf = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            SSLServerSocket serverSocket = (SSLServerSocket) ssf.createServerSocket(port);

            InetAddress ipaddr = InetAddress.getLocalHost();

            serverSocket.setEnabledProtocols(new String[] { "TLSv1.3", "TLSv1.2" });

            System.out.println("Secure Chat Server started at: " + ipaddr.getHostAddress() + ":" + port);


            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected: " + socket.getInetAddress().getHostAddress());
                Thread.startVirtualThread(() -> new ClientHandler(socket, roomManager).run());

                if (roomManager.getNumberRooms() > 0) {
                    System.out.println("Active rooms: " + roomManager.getNumberRooms());
                } else {
                    System.out.println("No active rooms.");
                }
                
            }
        
        } catch (IOException ex) {
            System.err.println("Server error: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            pool.shutdown();
        }
    }
}
