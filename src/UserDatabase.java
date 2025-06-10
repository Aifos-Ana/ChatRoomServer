import java.io.*;
import java.security.MessageDigest;
import java.util.HashMap;

public class UserDatabase {
    private static final String FILE_PATH = "users.dat";
    private static HashMap<String, String> users = loadUsers();

    public static void registerUser(String username, String password) throws Exception {
        if (users.containsKey(username)) {
            throw new Exception("User already exists!");
        }
        users.put(username, hashPassword(password));
        saveUsers();
    }

    public static boolean authenticateUser(String username, String password) throws Exception {
        return users.containsKey(username) && users.get(username).equals(hashPassword(password));
    }

    private static void saveUsers() throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
            oos.writeObject(users);
        }
    }

    private static HashMap<String, String> loadUsers() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            System.out.println("Creating new users.dat file...");
            return new HashMap<>();
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_PATH))) {
            Object obj = ois.readObject(); // Read object first
    
            // Check if obj is a HashMap
            if (obj instanceof HashMap<?, ?> map) {
                HashMap<String, String> users = new HashMap<>();
    
                // Ensure all keys & values are Strings
                for (Object key : map.keySet()) {
                    Object value = map.get(key);
                    if (key instanceof String && value instanceof String) {
                        users.put((String) key, (String) value);
                    }
                }
                return users;
            } else {
                System.out.println("Invalid data format in users.dat. Resetting file.");
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading users.dat, creating new file.");
        }
        return new HashMap<>();
    }
    
    
    private static String hashPassword(String password) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(password.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) sb.append(String.format("%02x", b));
        return sb.toString();
    }


    //DEBUGGING 
    public static void printUsers() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            System.out.println("No users registered yet.");
            return;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_PATH))) {
            Object obj = ois.readObject(); // Read as Object
    
            // Safe type check before casting
            if (obj instanceof HashMap<?, ?> map) {
                System.out.println("Registered Users:");
                
                // Print only valid String keys and values
                for (var entry : map.entrySet()) {
                    if (entry.getKey() instanceof String key && entry.getValue() instanceof String value) {
                        System.out.println("Username: " + key + ", Password Hash: " + value);
                    }
                }
            } else {
                System.out.println("Invalid data format in users.dat.");
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error reading users.dat.");
        }
    }
    
    

    public static void main(String[] args) {
        try {
            // Test: Create the file and add a test user
            registerUser("foo", "foo123");
            System.out.println("User 'testUser' registered.");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        printUsers();
    }
}
