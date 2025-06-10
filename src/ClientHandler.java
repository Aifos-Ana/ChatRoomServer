import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final RoomManager roomManager;
    private String currentToken;

    public ClientHandler(Socket socket, RoomManager roomManager) {
        this.socket = socket;
        this.roomManager = roomManager;
    }

    @Override
    public void run() {
        /*try {
            
            //socket.setSoTimeout(30000); // timeout de 30s para clientes lentos/inativos
        } catch (SocketException e) {
            e.printStackTrace();
            return;
        }*/

        Room room = null;
        ClientConnection client = null;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

            // Token exchange
            writer.println("TOKEN_REQUEST");
            String tokenInput = reader.readLine();
            if (tokenInput == null) {
                writer.println("No token received. Closing connection.");
                return;
            }

            SessionManager.Session session = null;
            String username = null;
            String roomName = null;

            if (!tokenInput.equalsIgnoreCase("NEW")) {
                session = SessionManager.getSession(tokenInput);
                if (session != null) {
                    currentToken = tokenInput;
                    username = session.getUsername();
                    roomName = session.getRoomName();
                    writer.println("RESUMED " + roomName);
                }
            }

            if (session == null) {
                // Autenticação
                writer.println("AUTH_REQUIRED");

                while (true) {
                    writer.println("Choose: 1 = Login | 2 = Register");
                    String choice = reader.readLine();
                    if (choice == null) return;

                    writer.println("Enter username:");
                    username = reader.readLine();
                    if (username == null) return;

                    writer.println("Enter password:");
                    String password = reader.readLine();
                    if (password == null) return;

                    try {
                        if ("1".equals(choice) && UserDatabase.authenticateUser(username, password)) {
                            writer.println("Login successful.");
                            break;
                        } else if ("2".equals(choice)) {
                            UserDatabase.registerUser(username, password);
                            writer.println("Registration successful. Logged in.");
                            break;
                        } else {
                            writer.println("Invalid credentials. Try again.");
                        }
                    } catch (Exception e) {
                        writer.println("Error: " + e.getMessage());
                    }
                }

                // Novo token
                currentToken = SessionManager.generateToken(username, "AI_Room");
                writer.println("TOKEN " + currentToken);
            }

            client = new ClientConnection(socket, writer, username);

            // Escolher sala
            if (roomName == null) {
                roomManager.listAllRooms(client);
                writer.println("Enter room name:");
                roomName = reader.readLine();
                if (roomName == null) return;
            }

            room = roomManager.getOrCreateRoom(roomName);

            if ("AI_Room".equals(roomName)) {
                writer.println("You are now chatting with the AI. Type your messages below:");
            }

            SessionManager.updateSessionRoom(currentToken, roomName);

            client.setRoom(room);
            room.join(client);

            String msg;

            try {
                while ((msg = reader.readLine()) != null) {
                    if (msg.equalsIgnoreCase("/leave")) {
                        writer.println("You have left the room.");
                        room.leave(client);

                        roomManager.listAllRooms(client);
                        writer.println("Enter room name:");
                        String newRoomName = reader.readLine();
                        if (newRoomName == null) break;

                        room = roomManager.getOrCreateRoom(newRoomName);
                        client.setRoom(room);
                        room.join(client);
                        SessionManager.updateSessionRoom(currentToken, newRoomName);
                        continue;

                    } else if (msg.equalsIgnoreCase("/quit")) {
                        writer.println("Goodbye!");
                        SessionManager.removeSession(currentToken);
                        room.leave(client);
                        break;

                    } else if (msg.equalsIgnoreCase("/list")) {
                        roomManager.listAllRooms(client);
                        continue;

                    } else if (msg.equalsIgnoreCase("/rooms")) {
                        writer.println("Number of rooms: " + roomManager.getNumberRooms());
                        continue;

                    } else if (msg.equalsIgnoreCase("/help")) {
                        writer.println("Available commands: /leave, /list, /quit");
                        writer.println("/leave: leaves the current room.");
                        writer.println("/list: lists all available rooms.");
                        writer.println("/quit: exits the chat application.");
                        writer.println("To type your message simply write it out and it will be sent to the room.");
                        continue;
                    }

                    room.broadcast(username + ": " + msg);

                    if (!msg.startsWith("/") && room.isAIRoom()) {
                        System.out.println("[DEBUG] AI room detected, calling AI with context...");
                        String aiResponse = AIIntegration.getAIResponse(room.getFullContext());
                        room.broadcast("Bot: " + aiResponse);
                    }
                }
            } catch (SocketTimeoutException e) {
                writer.println("Connection timed out due to inactivity.");
                System.out.println("Client timed out: " + username);
            }

        } catch (IOException e) {
            System.out.println("Client error: " + e.getMessage());
        } finally {
            try {
                if (room != null && client != null) {
                    room.leave(client);
                }
                if (currentToken != null) {
                    SessionManager.removeSession(currentToken);
                }
                socket.close();
            } catch (Exception ex) {
                System.out.println("Cleanup error: " + ex.getMessage());
            }
        }
    }
}
