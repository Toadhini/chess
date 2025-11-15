package ui;

import client.ServerFacade;
import model.AuthData;

import static ui.EscapeSequences.*;

/**
 * Handles all prelogin UI operations (before user is authenticated)
 */
public class PreloginUI {
    private final ServerFacade serverFacade;

    public PreloginUI(ServerFacade serverFacade) {
        this.serverFacade = serverFacade;
    }

    /**
     * Displays help text for prelogin commands
     */
    public String help() {
        StringBuilder sb = new StringBuilder();
        sb.append(SET_TEXT_BOLD).append("\nAvailable Commands:").append(RESET_TEXT_BOLD_FAINT).append("\n");
        sb.append(SET_TEXT_COLOR_BLUE).append("  register <username> <password> <email>").append(RESET_TEXT_COLOR)
                .append(" - Create a new account\n");
        sb.append(SET_TEXT_COLOR_BLUE).append("  login <username> <password>").append(RESET_TEXT_COLOR)
                .append(" - Login to your account\n");
        sb.append(SET_TEXT_COLOR_BLUE).append("  quit").append(RESET_TEXT_COLOR)
                .append(" - Exit the program\n");
        sb.append(SET_TEXT_COLOR_BLUE).append("  help").append(RESET_TEXT_COLOR)
                .append(" - Display this help message\n");
        return sb.toString();
    }

    /**
     * Registers a new user
     */
    public AuthData register(String username, String password, String email) throws Exception {
        return serverFacade.register(username, password, email);
    }

    /**
     * Logs in an existing user
     */
    public AuthData login(String username, String password) throws Exception {
        return serverFacade.login(username, password);
    }
}