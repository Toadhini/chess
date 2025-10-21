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
}