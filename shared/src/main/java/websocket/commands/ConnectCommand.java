package websocket.commands;

public class ConnectCommand extends UserGameCommand {
    private final ParticipationType participationType;

    public ConnectCommand(CommandType commandType, String authToken, Integer gameID, String username, ParticipationType participationType) {
        super(commandType, authToken, gameID, username);
        this.participationType = participationType;
    }

    public ParticipationType getParticipationType() {
        return participationType;
    }
}
