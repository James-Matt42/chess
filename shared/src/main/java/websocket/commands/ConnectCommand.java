package websocket.commands;

public class ConnectCommand extends UserGameCommand {
    private final String username;
    private final ParticipationType participationType;

    public ConnectCommand(CommandType commandType, String authToken, Integer gameID, String username, ParticipationType participationType) {
        super(commandType, authToken, gameID);
        this.username = username;
        this.participationType = participationType;
    }

    public String getUsername() {
        return username;
    }

    public ParticipationType getParticipationType() {
        return participationType;
    }
}
