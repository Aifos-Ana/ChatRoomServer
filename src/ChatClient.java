import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import javax.net.ssl.*;

public class ChatClient {
    private static String loadToken() {
        try (BufferedReader br = new BufferedReader(new FileReader("token.txt"))) {
            return br.readLine();
        } catch (IOException e) {
            return null;
        }
    }

    private static void saveToken(String token) {
        try (PrintWriter pw = new PrintWriter("token.txt")) {
            pw.println(token);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java ChatClient <host> <port>");
            return;
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);

        // SSL client setup
        System.setProperty("javax.net.ssl.trustStore", "keystore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "cpd2425");

        try {
            SSLSocketFactory sf = (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket socket = (SSLSocket) sf.createSocket(host, port);

            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 Scanner scanner = new Scanner(System.in)) {

                // Handle token
                String token = loadToken();
                if (token != null) {
                    out.println(token);
                    String response = in.readLine();
                    if (!response.startsWith("RESUMED")) {
                        token = null;
                        new File("token.txt").delete();
                    }
                }

                if (token == null) {
                    out.println("NEW");
                    String serverMsg;
                    boolean authComplete = false;

                    while (!authComplete && (serverMsg = in.readLine()) != null) {
                        System.out.println(serverMsg);

                        if (serverMsg.startsWith("TOKEN ")) {
                            token = serverMsg.split(" ")[1];
                            saveToken(token);
                            break;
                        }

                        if (serverMsg.contains("Choose:") || serverMsg.contains("username:") || serverMsg.contains("password:")) {
                            String userInput = scanner.nextLine();
                            out.println(userInput);
                        }
                    }
                }

                Thread readerThread = new Thread(() -> {
                    String line;
                    try {
                        while ((line = in.readLine()) != null) {
                            System.out.println(line);
                        }
                    } catch (IOException e) {
                        System.out.println("Connection closed.");
                    }
                });

                readerThread.start();

                while (scanner.hasNextLine()) {
                    String input = scanner.nextLine();
                    out.println(input);
                    if (input.equalsIgnoreCase("/quit")) break;
                }

            } // End of try-with-resources

        } catch (IOException e) {
            System.out.println("Connection error: " + e.getMessage());
        }
    }
}
