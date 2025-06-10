import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class RoomManager {
    private final Map<String, Room> rooms = new HashMap<>();
    private final ReentrantLock lock = new ReentrantLock();

    public RoomManager() {
        lock.lock();
        try {
            // Initialize the AI room
            rooms.put("AI_Room", new Room("AI_Room", true, null));
        } finally {
            lock.unlock();
        }
    }

    public Room getOrCreateRoom(String name) {
        System.out.println("Room: " + name);
        lock.lock();
        try {
            // Check existence before creating
            if (!rooms.containsKey(name)) {
                rooms.put(name, new Room(name)); // Non-AI room by default
            }
            return rooms.get(name);
        } finally {
            lock.unlock();
        }
    }

    public void removeClientFromAllRooms(ClientConnection client) {
        lock.lock();
        try {
            // Iterate safely over rooms
            for (Room room : rooms.values()) {
                room.leave(client);
            }
        } finally {
            lock.unlock();
        }
    }

    public void removeClientFromCurrRoom(ClientConnection client) {
        Room room = client.getRoom();
        if (room != null) {
            room.leave(client);
        }
    }

    public int getNumberRooms() {
        lock.lock();
        try {
            return rooms.size();
        } finally {
            lock.unlock();
        }
    }

    public void listAllRooms(ClientConnection client) {
        lock.lock();
        try {
            StringBuilder roomList = new StringBuilder("Available rooms: ");
            for (String roomName : rooms.keySet()) {
                roomList.append(roomName).append(", ");
            }
            if (!rooms.isEmpty()) {
                roomList.setLength(roomList.length() - 2); // Trim trailing ", "
            } else {
                roomList.append("No rooms available.");
            }
            client.sendMessage(roomList.toString());
        } finally {
            lock.unlock();
        }
    }

    public Room createAIRoom(String name, String prompt) {
        lock.lock();
        try {
            Room aiRoom = new Room(name, true, prompt);
            rooms.put(name, aiRoom);
            return aiRoom;
        } finally {
            lock.unlock();
        }
    }
}
