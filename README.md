# ChatRoomServer: Java Distributed Chat System with AI Rooms

This project is a distributed client-server chat system implemented in Java SE 21. It supports multi-user chat with virtual threads, user authentication, AI-integrated rooms, and reconnection via session tokens.

## Features

- User registration & login with SHA-256 hashed passwords.
- Session-based authentication using UUID tokens.
- AI-powered chat rooms using local LLM (via Ollama).
- Lightweight concurrency using **virtual threads**.
- Multiple chat rooms with live message broadcasting.
- Secure communication via `javax.net.ssl` (SSL/TLS).
- Token-based reconnection without re-login.
- Thread-safe room management using locks (`ReentrantLock`).

---

## Getting Started

### Requirements
- Java SE 21 or later
- Ollama installed and running locally
- A model pulled with Ollama (in this project we use `llama3`):
  ```bash
     #To start vm 
          sudo docker run -d -v ollama:/root/.ollama -p 11434:11434 --name ollama14 ollama/ollama 

     #To test 
          sudo docker exec -it ollama14 ollama run llama3 
  ```
  
---

### Build Instructions

```bash
javac *.java
``` 
---

### SSL Configuration (for secure communication)
You must first generate a self-signed certificate:
```bash
keytool -genkeypair -alias chatserver -keyalg RSA -keysize 2048 \ -keystore keystore.jks -validity 365
```
Use ```cpd2425``` as a password. Use ```localhost``` as the common name when prompted.

### Running the Server (SSL enabled)
Start the server on a chosen port (e.g., 12345):
```bash
java -Djavax.net.ssl.keyStore=keystore.jks \
     -Djavax.net.ssl.keyStorePassword=cpd2425 \
     TimeServer 12345
``` 
You should see:
```bash
Secure Chat Server started on port: 12345
```
---
### Running the Client (SSL enabled)
In a separate terminal or on another machine:
```bash
java -Djavax.net.ssl.trustStore=keystore.jks \
     -Djavax.net.ssl.trustStorePassword=cpd2425 \
     ChatClient <your-ip> 12345
```
---

## Authentication flow
- If a token is available (token.txt), the client resumes the session.
- Otherwise:
    - Choose to login or register.
    - Enter credentials.
    - Token is saved for next session.

---
## Available commands (in chat):
| Command  | Description                           |
| -------- | ------------------------------------- |
| `/leave` | Leave current room and choose another |
| `/list`  | List all available chat rooms         |
| `/rooms` | Show number of active rooms           |
| `/quit`  | Disconnect from the server            |
| `/help`  | Show all available commands           |

---
## AI Rooms
- Join or create a room named AI_Room (or any name flagged as AI-enabled, writing for example "newRoom AI" which will be created as newRoom_AI).
- Messages sent to AI rooms are forwarded to a local LLM (via HTTP to Ollama).
- Bot replies are automatically injected into the room with **Bot:** prefix.

---
## Project Structure
| File                    | Purpose                               |
| ----------------------- | ------------------------------------- |
| `TimeServer.java`       | Launches the multithreaded server     |
| `ChatClient.java`       | Interactive client app                |
| `UserDatabase.java`     | Handles user auth & password hashing  |
| `SessionManager.java`   | Manages session tokens                |
| `RoomManager.java`      | Tracks all active rooms               |
| `Room.java`             | Handles per-room logic & broadcasting |
| `ClientHandler.java`    | Handles a connected client session    |
| `ClientConnection.java` | Abstraction for client-side state     |
| `AIIntegration.java`    | Calls Ollama to get AI responses      |
---
## Notes

- Tokens are saved to a local token.txt file for session recovery.
- Passwords are stored hashed (not plaintext) in users.dat.
- Ollama must be accessible at http://localhost:11434.
- If token is expired or invalid, a new login is requested.

---

## Fault Tolerance
- If the client disconnects unexpectedly:
    - It can reconnect using the saved token.
    - The server resumes the session in the previous room.
    - No need to re-authenticate or rejoin the room manually.

---

> Project developed for Parallel and Distributed Computing course @FEUP
