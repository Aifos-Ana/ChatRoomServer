import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class Room {
    private final String name;
    private final boolean isAI;
    private final String prompt;
    private final List<ClientConnection> clients = new ArrayList<>();
    private final List<String> history = new ArrayList<>();
    private final ReentrantLock lock = new ReentrantLock();

    public Room(String name) {
        this(name, false, null);
    }

    public Room(String name, boolean isAI, String prompt) {
        this.name = name;
        this.isAI = isAI;
        this.prompt = prompt;
    }

    public void join(ClientConnection client) {
        lock.lock();
        try {
            clients.add(client);
        } finally {
            lock.unlock();
        }
        broadcast("[" + client.getUsername() + " has joined the room.]");
    }

    public void leave(ClientConnection client) {
        lock.lock();
        try {
            clients.remove(client);
        } finally {
            lock.unlock();
        }
        broadcast("[" + client.getUsername() + " has left the room.]");
    }

    public void broadcast(String message) {
        lock.lock();
        try {
            history.add(message);
        } finally {
            lock.unlock();
        }

        List<ClientConnection> clientsCopy;
        lock.lock();
        try {
            clientsCopy = new ArrayList<>(clients);
        } finally {
            lock.unlock();
        }

        for (ClientConnection client : clientsCopy) {
            client.sendMessage("[" + name + "] " + message);
        }
    }

    public String getName() {
        return name;
    }

    public boolean isAIRoom() {
        return isAI;
    }

    public String getPrompt() {
        return prompt;
    }


    public String getFullContext() {
        List<String> historyCopy;
        lock.lock();
        try {
            historyCopy = new ArrayList<>(history);
        } finally {
            lock.unlock();
        }

        StringBuilder context = new StringBuilder(prompt != null ? prompt + "\n" : "");
        for (String msg : historyCopy) {
            context.append(msg).append("\n");
        }
        return context.toString();
    }
}
