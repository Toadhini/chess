package service;

import model.*;
import dataaccess.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class GameServiceTest {

    private UserDAO userDAO;
    private AuthDAO authDAO;
    private GameDAO gameDAO;
    private GameService gameService;
    private String validAuthToken;

    @BeforeEach
    public void setUp() throws DataAccessException {
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        gameDAO = new MemoryGameDAO();
        gameService = new GameService(authDAO, gameDAO);

        // Create test user and auth token
        userDAO.createUser(new UserData("testUser", "password", "test@email.com"));
        validAuthToken = "validToken123";
        authDAO.createAuth(new AuthData(validAuthToken, "testUser"));
    }

    @Test
    @DisplayName("Create Game Successfully")
    public void createGameSuccess() throws DataAccessException {
        CreateGameResult result = gameService.createGame("TestGame", validAuthToken);

        assertNotNull(result);
        assertNotNull(result.gameID());
        assertTrue(result.gameID() > 0);

        // Verify game was created
        GameData game = gameDAO.getGame(result.gameID());
        assertNotNull(game);
        assertEquals("TestGame", game.gameName());
    }

    @Test
    @DisplayName("Create Game with Invalid Auth Fails")
    public void createGameUnauthorized() {
        DataAccessException exception = assertThrows(DataAccessException.class,
                () -> gameService.createGame("TestGame", "invalidToken"));

        assertTrue(exception.getMessage().contains("unauthorized"));
    }

    @Test
    @DisplayName("Create Game with Null Name Fails")
    public void createGameBadRequest() {
        DataAccessException exception = assertThrows(DataAccessException.class,
                () -> gameService.createGame(null, validAuthToken));

        assertTrue(exception.getMessage().contains("bad request"));
    }

    @Test
    @DisplayName("List Games Successfully")
    public void listGamesSuccess() throws DataAccessException {
        // Create some games
        gameService.createGame("Game1", validAuthToken);
        gameService.createGame("Game2", validAuthToken);

        ListGamesResult result = gameService.listGames(validAuthToken);

        assertNotNull(result);
        assertNotNull(result.games());
        assertEquals(2, result.games().size());
    }

    @Test
    @DisplayName("List Games with Invalid Auth Fails")
    public void listGamesUnauthorized() {
        DataAccessException exception = assertThrows(DataAccessException.class,
                () -> gameService.listGames("invalidToken"));

        assertTrue(exception.getMessage().contains("unauthorized"));
    }

    @Test
    @DisplayName("Join Game Successfully")
    public void joinGameSuccess() throws DataAccessException {
        // Create a game
        CreateGameResult createResult = gameService.createGame("TestGame", validAuthToken);
        int gameID = createResult.gameID();

        // Join as white
        assertDoesNotThrow(() -> gameService.joinGame(gameID, "WHITE", validAuthToken));

        // Verify player was added
        GameData game = gameDAO.getGame(gameID);
        assertEquals("testUser", game.whiteUsername());
        assertNull(game.blackUsername());
    }

    @Test
    @DisplayName("Join Game with Invalid Auth Fails")
    public void joinGameUnauthorized() throws DataAccessException {
        CreateGameResult createResult = gameService.createGame("TestGame", validAuthToken);

        DataAccessException exception = assertThrows(DataAccessException.class,
                () -> gameService.joinGame(createResult.gameID(), "WHITE", "invalidToken"));

        assertTrue(exception.getMessage().contains("unauthorized"));
    }

    @Test
    @DisplayName("Join Already Taken Color Fails")
    public void joinGameAlreadyTaken() throws DataAccessException {
        // Create game and join as white
        CreateGameResult createResult = gameService.createGame("TestGame", validAuthToken);
        gameService.joinGame(createResult.gameID(), "WHITE", validAuthToken);

        // Create second user
        String secondToken = "secondToken456";
        userDAO.createUser(new UserData("secondUser", "pass", "second@test.com"));
        authDAO.createAuth(new AuthData(secondToken, "secondUser"));

        // Try to join same color
        DataAccessException exception = assertThrows(DataAccessException.class,
                () -> gameService.joinGame(createResult.gameID(), "WHITE", secondToken));

        assertTrue(exception.getMessage().contains("already taken"));
    }

    @Test
    @DisplayName("Join Game with Bad Request Fails")
    public void joinGameBadRequest() throws DataAccessException {
        CreateGameResult createResult = gameService.createGame("TestGame", validAuthToken);

        // Null gameID
        DataAccessException exception1 = assertThrows(DataAccessException.class,
                () -> gameService.joinGame(null, "WHITE", validAuthToken));
        assertTrue(exception1.getMessage().contains("bad request"));

        // Null color
        DataAccessException exception2 = assertThrows(DataAccessException.class,
                () -> gameService.joinGame(createResult.gameID(), null, validAuthToken));
        assertTrue(exception2.getMessage().contains("bad request"));

        // Invalid color
        DataAccessException exception3 = assertThrows(DataAccessException.class,
                () -> gameService.joinGame(createResult.gameID(), "GREEN", validAuthToken));
        assertTrue(exception3.getMessage().contains("bad request"));
    }
}