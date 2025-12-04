package websocket.messages;

// Once more only necessary because of the stupid way in which the tests were made
// Otherwise I would just have one class representing server messages
public class ServerErrorMessage extends ServerMessage {

    private final String errorMessage;

    public ServerErrorMessage(ServerMessageType type, String errorMessage) {
        super(type);
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
