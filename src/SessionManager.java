import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SessionManager {
    private static final long TOKEN_EXPIRATION_MS = 1; // 1 hour
    private static final Map<String, Session> sessions = new HashMap<>();

    public static class Session {
        private final String username;
        private String roomName;
        private long expirationTime;

        public Session(String username, String roomName) {
            this.username = username;
            this.roomName = roomName;
            this.expirationTime = System.currentTimeMillis() + TOKEN_EXPIRATION_MS;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expirationTime;
        }

        public void updateExpiration() {
            this.expirationTime = System.currentTimeMillis() + TOKEN_EXPIRATION_MS;
        }

        public String getUsername() {
            return username;
        }

        public String getRoomName() {
            return roomName;
        }

        public void setRoomName(String roomName) {
            this.roomName = roomName;
            updateExpiration();
        }
    }

    public static synchronized String generateToken(String username, String roomName) {
        String token = UUID.randomUUID().toString();
        sessions.put(token, new Session(username, roomName));
        return token;
    }

    public static synchronized Session getSession(String token) {
        Session session = sessions.get(token);
        if (session == null) return null;
        if (session.isExpired()) {
            sessions.remove(token);
            return null;
        }
        return session;
    }

    public static synchronized void updateSessionRoom(String token, String roomName) {
        Session session = sessions.get(token);
        if (session != null) session.setRoomName(roomName);
    }

    public static synchronized void removeSession(String token) {
        sessions.remove(token);
    }
}
