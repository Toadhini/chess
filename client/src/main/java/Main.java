import chess.*;
import ui.ChessClient;

public class Main {
    public static void main(String[] args) {
        // Default port is 8080, but you can change it if needed
        int port = 8080;
        ChessClient client = new ChessClient(port);
        client.run();
    }
}
