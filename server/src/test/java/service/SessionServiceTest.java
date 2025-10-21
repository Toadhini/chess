package service;

import model.*;
import dataaccess.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class SessionServiceTest {

    private UserDAO userDAO;
    private AuthDAO authDAO;
    private GameDAO gameDAO;
    private SessionService sessionService;
    private UserService userService;

    @BeforeEach
    public void setUp() throws DataAccessException {
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        gameDAO = new MemoryGameDAO();
        sessionService = new SessionService(authDAO, userDAO, gameDAO);
        userService = new UserService(userDAO, authDAO);

        // Create a test user for login tests
        userService.register(new UserData("existingUser", "password123", "user@test.com"));
    }

    @Test
    @DisplayName("Login Existing User Successfully")
    public void loginSuccess() throws DataAccessException {
        LoginSessionResult result = sessionService.login("existingUser", "password123");

        assertNotNull(result);
        assertEquals("existingUser", result.username());
        assertNotNull(result.authToken());

        // Verify auth token was created
        AuthData storedAuth = authDAO.getAuth(result.authToken());
        assertNotNull(storedAuth);
        assertEquals("existingUser", storedAuth.username());
    }

    @Test
    @DisplayName("Login with Wrong Password Fails")
    public void loginUnauthorized() {
        DataAccessException exception = assertThrows(DataAccessException.class,
                () -> sessionService.login("existingUser", "wrongPassword"));

        assertTrue(exception.getMessage().contains("unauthorized"));
    }

    @Test
    @DisplayName("Login Non-existent User Fails")
    public void loginNonExistentUser() {
        DataAccessException exception = assertThrows(DataAccessException.class,
                () -> sessionService.login("nonExistentUser", "password"));

        assertTrue(exception.getMessage().contains("unauthorized"));
    }
}