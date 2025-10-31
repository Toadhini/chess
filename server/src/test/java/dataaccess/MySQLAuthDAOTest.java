package dataaccess;

import model.AuthData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MySQLAuthDAOTest {
    private static MySQLAuthDAO authDAO;

    @BeforeAll
    public static void setUp() throws DataAccessException {
        authDAO = new MySQLAuthDAO();
        authDAO.clear();
    }

    @BeforeEach
    public void clearDatabase() throws DataAccessException {
        authDAO.clear();
    }

    // createAuth Tests
    @Test
    @Order(1)
    @DisplayName("Create Auth - Positive")
    public void createAuthPositive() throws DataAccessException {
        AuthData auth = new AuthData("token123", "testuser");
        assertDoesNotThrow(() -> authDAO.createAuth(auth));

        // Verify auth was created
        AuthData retrieved = authDAO.getAuth("token123");
        assertNotNull(retrieved);
        assertEquals("token123", retrieved.authToken());
        assertEquals("testuser", retrieved.username());
    }

    @Test
    @Order(2)
    @DisplayName("Create Auth - Negative (Duplicate Token)")
    public void createAuthNegative() throws DataAccessException {
        AuthData auth1 = new AuthData("duplicateToken", "user1");
        AuthData auth2 = new AuthData("duplicateToken", "user2");

        authDAO.createAuth(auth1);

        // Attempting to create duplicate auth token should throw exception
        assertThrows(DataAccessException.class, () -> authDAO.createAuth(auth2));
    }

    // getAuth tests
    @Test
    @Order(3)
    @DisplayName("Get Auth - Positive")
    public void getAuthPositive() throws DataAccessException{
        AuthData auth = new AuthData("getToken", "getUser");
        authDAO.createAuth(auth);

        AuthData retrieved = authDAO.getAuth("getToken");
        assertNotNull(retrieved);
        assertEquals("getToken", retrieved.authToken());
        assertEquals("getUser", retrieved.username());
    }

    @Test
    @Order(4)
    @DisplayName("Get Auth - Negative")
    public void getAuthNegative() throws DataAccessException{
        AuthData retrieved = authDAO.getAuth("nonexistentToken");
        assertNull(retrieved);
    }

    // deleteAuth Tests
    @Test
    @Order(5)
    @DisplayName("Delete Auth - Positive")
    public void deleteAuthPositive() throws DataAccessException {
        AuthData auth = new AuthData("deleteToken", "deleteUser");
        authDAO.createAuth(auth);

        // Verify auth exists
        assertNotNull(authDAO.getAuth("deleteToken"));

        // Delete auth
        assertDoesNotThrow(() -> authDAO.deleteAuth("deleteToken"));

        // Verify auth is gone
        assertNull(authDAO.getAuth("deleteToken"));
    }

    @Test
    @Order(6)
    @DisplayName("Delete Auth - Negative (Delete Non-existent)")
    public void deleteAuthNegative() throws DataAccessException {
        // Deleting non-existent auth should not throw exception
        assertDoesNotThrow(() -> authDAO.deleteAuth("nonexistentToken"));
    }

}