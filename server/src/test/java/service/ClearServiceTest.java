package service;

import model.*;
import dataaccess.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class ClearServiceTest {

    private UserDAO userDAO;
    private AuthDAO authDAO;
    private GameDAO gameDAO;
    private ClearService clearService;

    @BeforeEach
    public void setUp() {
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        gameDAO = new MemoryGameDAO();
        clearService = new ClearService(userDAO, authDAO, gameDAO);
    }

    @Test
    @DisplayName("Clear All Data Successfully")
    public void clearSuccess() throws DataAccessException {
        // Add some test data
        userDAO.createUser(new UserData("user1", "pass1", "email1@test.com"));
        userDAO.createUser(new UserData("user2", "pass2", "email2@test.com"));
        authDAO.createAuth(new AuthData("token1", "user1"));
        authDAO.createAuth(new AuthData("token2", "user2"));
        gameDAO.createGame("TestGame");

        // Verify data exists
        assertNotNull(userDAO.getUser("user1"));
        assertNotNull(authDAO.getAuth("token1"));

        // Clear all data
        assertDoesNotThrow(() -> clearService.clear());

        // Verify all data is cleared
        assertNull(userDAO.getUser("user1"));
        assertNull(userDAO.getUser("user2"));
        assertNull(authDAO.getAuth("token1"));
        assertNull(authDAO.getAuth("token2"));
        assertTrue(gameDAO.listGames().isEmpty());
    }
}