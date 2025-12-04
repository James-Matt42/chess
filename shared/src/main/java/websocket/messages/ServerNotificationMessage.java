package websocket.messages;

public class ServerNotificationMessage extends ServerMessage {

    private final String message;

    public ServerNotificationMessage(ServerMessageType type, String message) {
        super(type);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
