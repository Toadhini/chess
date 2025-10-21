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

    @Test
    @DisplayName("Login with Missing Credentials Fails")
    public void loginBadRequest() {
        // Null username
        DataAccessException exception1 = assertThrows(DataAccessException.class,
                () -> sessionService.login(null, "password"));
        assertTrue(exception1.getMessage().contains("bad request"));

        // Null password
        DataAccessException exception2 = assertThrows(DataAccessException.class,
                () -> sessionService.login("user", null));
        assertTrue(exception2.getMessage().contains("bad request"));
    }

    @Test
    @DisplayName("Logout Successfully")
    public void logoutSuccess() throws DataAccessException {
        // Login to get auth token
        LoginSessionResult loginResult = sessionService.login("existingUser", "password123");
        String authToken = loginResult.authToken();

        // Verify token exists
        assertNotNull(authDAO.getAuth(authToken));

        // Logout
        assertDoesNotThrow(() -> sessionService.logout(authToken));

        // Verify token was deleted
        assertNull(authDAO.getAuth(authToken));
    }

    @Test
    @DisplayName("Logout with Invalid Token Fails")
    public void logoutUnauthorized() {
        DataAccessException exception = assertThrows(DataAccessException.class,
                () -> sessionService.logout("invalidToken"));

        assertTrue(exception.getMessage().contains("unauthorized"));
    }

    @Test
    @DisplayName("Logout with Null Token Fails")
    public void logoutBadRequest() {
        DataAccessException exception = assertThrows(DataAccessException.class,
                () -> sessionService.logout(null));

        assertTrue(exception.getMessage().contains("bad request"));
    }
}