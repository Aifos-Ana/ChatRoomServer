import java.io.PrintWriter;
import java.net.Socket;

public class ClientConnection {
    private final Socket socket;
    private PrintWriter out;
    private final String username;
    private Room room;

    public ClientConnection(Socket socket, PrintWriter out, String username) {
        this.socket = socket;
        this.out = out;
        this.username = username;
    }

    public void updateOutput(PrintWriter newOut) {
        this.out = newOut;
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    public String getUsername() {
        return username;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }
}