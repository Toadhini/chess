package ui;

import client.ServerFacade;
import java.util.Scanner;
import static ui.EscapeSequences.*;

public class ChessClient {
    private final ServerFacade serverFacade;
    private final Scanner scanner;
    private String authToken = null;
    private String username = null;
    private State state = State.PRELOGIN;

    private enum State{
        PRELOGIN,
        POSTLOGIN
    }

    public ChessClient(int port){
        this.serverFacade = new ServerFacade(port);
        this.scanner = new Scanner(System.in);
    }

}
