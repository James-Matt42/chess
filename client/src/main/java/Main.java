import chess.*;
import client.ServerFacade;
import server.Server;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Client: " + piece);

        Server server = new Server();
        int port = server.run(8080);
        ServerFacade facade = new ServerFacade(port);

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equals("quit")) {
            printPrompt();
            String line = scanner.nextLine();
            System.out.format("You typed: %s\n", line);
            result = line;
        }
        server.stop();
    }

    public void help() {
//        Print out all the different help options
    }

    static private void printPrompt() {
        System.out.print(">> ");
    }
}