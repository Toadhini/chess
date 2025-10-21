package service;

import model.*;
import dataaccess.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

    private UserDAO userDAO;
    private AuthDAO authDAO;
    private UserService userService;

    @BeforeEach
    public void setUp() {
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        userService = new UserService(userDAO, authDAO);
    }

    @Test
    @DisplayName("Register New User Successfully")
    public void registerSuccess() throws DataAccessException {
        UserData newUser = new UserData("testUser", "testPass", "test@email.com");

        RegisterResult result = userService.register(newUser);

        assertNotNull(result);
        assertEquals("testUser", result.username());
        assertNotNull(result.authToken());

        // Verify user was created in database
        UserData storedUser = userDAO.getUser("testUser");
        assertNotNull(storedUser);
        assertEquals("testUser", storedUser.username());

        // Verify auth token was created
        AuthData storedAuth = authDAO.getAuth(result.authToken());
        assertNotNull(storedAuth);
        assertEquals("testUser", storedAuth.username());
    }

    @Test
    @DisplayName("Register Duplicate User Fails")
    public void registerDuplicate() throws DataAccessException {
        UserData user = new UserData("duplicate", "pass", "email@test.com");

        // Register user first time
        userService.register(user);

        // Try to register same username again
        DataAccessException exception = assertThrows(DataAccessException.class,
                () -> userService.register(user));

        assertTrue(exception.getMessage().contains("already taken"));
    }

    @Test
    @DisplayName("Register with Missing Fields Fails")
    public void registerBadRequest() {
        // Missing password
        UserData invalidUser1 = new UserData("user", null, "email@test.com");
        DataAccessException exception1 = assertThrows(DataAccessException.class,
                () -> userService.register(invalidUser1));
        assertTrue(exception1.getMessage().contains("bad request"));

        // Missing email
        UserData invalidUser2 = new UserData("user", "pass", null);
        DataAccessException exception2 = assertThrows(DataAccessException.class,
                () -> userService.register(invalidUser2));
        assertTrue(exception2.getMessage().contains("bad request"));
    }
}