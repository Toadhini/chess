package dataaccess;

import chess.ChessGame;
import model.GameData;
import org.junit.jupiter.api.*;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MySQLGameDAOTest {
    private static MySQLGameDAO gameDAO;

    @BeforeAll
    public static void setUp() throws DataAccessException {
        gameDAO = new MySQLGameDAO();
        gameDAO.clear();
    }

    @BeforeEach
    public void clearDatabase() throws DataAccessException {
        gameDAO.clear();
    }

    // createGame Tests
    @Test
    @Order(1)
    @DisplayName("Create Game - Positive")
    public void createGamePositive() throws DataAccessException {
        int gameID = gameDAO.createGame("Test Game");
        assertTrue(gameID > 0);

        // Verify game was created
        GameData retrieved = gameDAO.getGame(gameID);
        assertNotNull(retrieved);
        assertEquals(gameID, retrieved.gameID());
        assertEquals("Test Game", retrieved.gameName());
        assertNull(retrieved.whiteUsername());
        assertNull(retrieved.blackUsername());
        assertNotNull(retrieved.game());
    }

    @Test
    @Order(2)
    @DisplayName("Create Game - Negative (Null Game Name)")
    public void createGameNegative() {
        // Creating game with null name should throw exception
        assertThrows(DataAccessException.class, () -> gameDAO.createGame(null));
    }


}