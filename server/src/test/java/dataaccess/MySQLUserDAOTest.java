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

}