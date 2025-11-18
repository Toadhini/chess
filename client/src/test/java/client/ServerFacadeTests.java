package client;

import model.*;
import org.junit.jupiter.api.*;
import server.Server;

import static org.junit.jupiter.api.Assertions.*;

public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade(port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    public void clearDatabase() throws Exception {
        // Clear the database before each test
        // You'll need to add a clear method to ServerFacade or call the server's clear endpoint
        // For now, we'll work around it by using unique usernames
    }

    // ========== REGISTER TESTS ==========

    @Test
    @DisplayName("Register Success")
    public void registerSuccess() throws Exception {
        AuthData authData = facade.register("testuser1", "password", "test1@email.com");

        assertNotNull(authData, "AuthData should not be null");
        assertNotNull(authData.authToken(), "Auth token should not be null");
        assertTrue(authData.authToken().length() > 10, "Auth token should be substantial");
        assertEquals("testuser1", authData.username(), "Username should match");
    }

    @Test
    @DisplayName("Register Duplicate Username")
    public void registerDuplicateUsername() throws Exception {
        // Register first user
        facade.register("duplicateuser", "password", "dup@email.com");

        // Try to register same username again
        Exception exception = assertThrows(Exception.class, () -> {
            facade.register("duplicateuser", "password2", "dup2@email.com");
        });

        assertTrue(exception.getMessage().contains("already taken") ||
                        exception.getMessage().contains("403"),
                "Should indicate username is taken");
    }

    // ========== LOGIN TESTS ==========

    @Test
    @DisplayName("Login Success")
    public void loginSuccess() throws Exception {
        // Register a user first
        facade.register("loginuser", "loginpass", "login@email.com");

        // Now login with same credentials
        AuthData authData = facade.login("loginuser", "loginpass");

        assertNotNull(authData, "AuthData should not be null");
        assertNotNull(authData.authToken(), "Auth token should not be null");
        assertEquals("loginuser", authData.username(), "Username should match");
    }

    @Test
    @DisplayName("Login Wrong Password")
    public void loginWrongPassword() throws Exception {
        // Register a user
        facade.register("passuser", "correctpass", "pass@email.com");

        // Try to login with wrong password
        Exception exception = assertThrows(Exception.class, () -> {
            facade.login("passuser", "wrongpass");
        });

        assertTrue(exception.getMessage().contains("unauthorized") ||
                        exception.getMessage().contains("401"),
                "Should indicate unauthorized");
    }

    // ========== LOGOUT TESTS ==========

    @Test
    @DisplayName("Logout Success")
    public void logoutSuccess() throws Exception {
        // Register and get auth token
        AuthData authData = facade.register("logoutuser", "password", "logout@email.com");

        // Logout should not throw exception
        assertDoesNotThrow(() -> {
            facade.logout(authData.authToken());
        });
    }

    @Test
    @DisplayName("Logout Invalid Token")
    public void logoutInvalidToken() {
        // Try to logout with invalid token
        Exception exception = assertThrows(Exception.class, () -> {
            facade.logout("invalid-token-12345");
        });

        assertTrue(exception.getMessage().contains("unauthorized") ||
                        exception.getMessage().contains("401"),
                "Should indicate unauthorized");
    }

    // ========== LIST GAMES TESTS ==========

    @Test
    @DisplayName("List Games Success")
    public void listGamesSuccess() throws Exception {
        // Register and login
        AuthData authData = facade.register("listuser", "password", "list@email.com");

        // Create a couple games
        facade.createGame(authData.authToken(), "Game1");
        facade.createGame(authData.authToken(), "Game2");

        // List games
        ListGamesResult result = facade.listGames(authData.authToken());

        assertNotNull(result, "Result should not be null");
        assertNotNull(result.games(), "Games list should not be null");
        assertTrue(result.games().size() >= 2, "Should have at least 2 games");
    }

    @Test
    @DisplayName("List Games Unauthorized")
    public void listGamesUnauthorized() {
        // Try to list games with invalid token
        Exception exception = assertThrows(Exception.class, () -> {
            facade.listGames("invalid-token");
        });

        assertTrue(exception.getMessage().contains("unauthorized") ||
                        exception.getMessage().contains("401"),
                "Should indicate unauthorized");
    }

    // ========== CREATE GAME TESTS ==========

    @Test
    @DisplayName("Create Game Success")
    public void createGameSuccess() throws Exception {
        // Register and login
        AuthData authData = facade.register("createuser", "password", "create@email.com");

        // Create a game
        CreateGameResult result = facade.createGame(authData.authToken(), "MyNewGame");

        assertNotNull(result, "Result should not be null");
        assertNotNull(result.gameID(), "Game ID should not be null");
        assertTrue(result.gameID() > 0, "Game ID should be positive");
    }

    @Test
    @DisplayName("Create Game Unauthorized")
    public void createGameUnauthorized() {
        // Try to create game with invalid token
        Exception exception = assertThrows(Exception.class, () -> {
            facade.createGame("invalid-token", "UnauthorizedGame");
        });

        assertTrue(exception.getMessage().contains("unauthorized") ||
                        exception.getMessage().contains("401"),
                "Should indicate unauthorized");
    }

    // ========== JOIN GAME TESTS ==========

    @Test
    @DisplayName("Join Game Success")
    public void joinGameSuccess() throws Exception {
        // Register and login
        AuthData authData = facade.register("joinuser", "password", "join@email.com");

        // Create a game
        CreateGameResult game = facade.createGame(authData.authToken(), "JoinTestGame");

        // Join the game as WHITE
        assertDoesNotThrow(() -> {
            facade.joinGame(authData.authToken(), game.gameID(), "WHITE");
        });
    }

    @Test
    @DisplayName("Join Game Color Already Taken")
    public void joinGameColorTaken() throws Exception {
        // Register two users
        AuthData user1 = facade.register("joinuser1", "password", "join1@email.com");
        AuthData user2 = facade.register("joinuser2", "password", "join2@email.com");

        // Create a game
        CreateGameResult game = facade.createGame(user1.authToken(), "TakenColorGame");

        // User1 joins as WHITE
        facade.joinGame(user1.authToken(), game.gameID(), "WHITE");

        // User2 tries to join as WHITE (should fail)
        Exception exception = assertThrows(Exception.class, () -> {
            facade.joinGame(user2.authToken(), game.gameID(), "WHITE");
        });

        assertTrue(exception.getMessage().contains("already taken") ||
                        exception.getMessage().contains("403"),
                "Should indicate color is taken");
    }
}