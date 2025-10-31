package dataaccess;

import model.UserData;
import org.junit.jupiter.api.*;
import org.mindrot.jbcrypt.BCrypt;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MySQLUserDAOTest {
    private static MySQLUserDAO userDAO;

    @BeforeAll
    public static void setUp() throws DataAccessException {
        userDAO = new MySQLUserDAO();
        userDAO.clear();
    }

    @BeforeEach
    public void clearDatabase() throws DataAccessException {
        userDAO.clear();
    }

    // createUser Tests
    @Test
    @Order(1)
    @DisplayName("Create User - Positive")
    public void createUserPositive() throws DataAccessException {
        UserData user = new UserData("testuser", "password123", "test@email.com");
        assertDoesNotThrow(() -> userDAO.createUser(user));

        // Verify user was created
        UserData retrieved = userDAO.getUser("testuser");
        assertNotNull(retrieved);
        assertEquals("testuser", retrieved.username());
        assertEquals("test@email.com", retrieved.email());
        // Verify password is hashed
        assertTrue(BCrypt.checkpw("password123", retrieved.password()));
    }

    @Test
    @Order(2)
    @DisplayName("Create User - Negative (Duplicate Username)")
    public void createUserNegative() throws DataAccessException {
        UserData user1 = new UserData("duplicate", "password1", "email1@test.com");
        UserData user2 = new UserData("duplicate", "password2", "email2@test.com");

        userDAO.createUser(user1);

        // Attempting to create duplicate user should throw exception
        assertThrows(DataAccessException.class, () -> userDAO.createUser(user2));
    }

    // getUser Tests
    @Test
    @Order(3)
    @DisplayName("Get User - Positive")
    public void getUserPositive() throws DataAccessException {
        UserData user = new UserData("gettest", "mypassword", "get@test.com");
        userDAO.createUser(user);

        UserData retrieved = userDAO.getUser("gettest");
        assertNotNull(retrieved);
        assertEquals("gettest", retrieved.username());
        assertEquals("get@test.com", retrieved.email());
        assertTrue(BCrypt.checkpw("mypassword", retrieved.password()));
    }

    @Test
    @Order(4)
    @DisplayName("Get User - Negative (User Not Found)")
    public void getUserNegative() throws DataAccessException {
        UserData retrieved = userDAO.getUser("nonexistent");
        assertNull(retrieved);
    }

    @Test
    @Order(5)
    @DisplayName("Clear Users - Positive")
    public void clearUsersPositive() throws DataAccessException {
        // Add multiple users
        userDAO.createUser(new UserData("user1", "pass1", "email1@test.com"));
        userDAO.createUser(new UserData("user2", "pass2", "email2@test.com"));
        userDAO.createUser(new UserData("user3", "pass3", "email3@test.com"));

        // Verify users exist
        assertNotNull(userDAO.getUser("user1"));
        assertNotNull(userDAO.getUser("user2"));

        // Clear all users
        assertDoesNotThrow(() -> userDAO.clear());

        // Verify all users are gone
        assertNull(userDAO.getUser("user1"));
        assertNull(userDAO.getUser("user2"));
        assertNull(userDAO.getUser("user3"));
    }

    @Test
    @Order(6)
    @DisplayName("Clear Users - Negative (Clear Empty Table)")
    public void clearUsersNegative() throws DataAccessException {
        // Clear already empty table should not throw exception
        userDAO.clear();
        assertDoesNotThrow(() -> userDAO.clear());

        // Verify still empty
        assertNull(userDAO.getUser("anyuser"));
    }

}