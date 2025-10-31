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

    // getGame Tests
    @Test
    @Order(3)
    @DisplayName("Get Game - Positive")
    public void getGamePositive() throws DataAccessException {
        int gameID = gameDAO.createGame("Get Test Game");

        GameData retrieved = gameDAO.getGame(gameID);
        assertNotNull(retrieved);
        assertEquals(gameID, retrieved.gameID());
        assertEquals("Get Test Game", retrieved.gameName());
        assertNotNull(retrieved.game());
    }

    @Test
    @Order(4)
    @DisplayName("Get Game - Negative (Game Not Found)")
    public void getGameNegative() throws DataAccessException {
        GameData retrieved = gameDAO.getGame(99999);
        assertNull(retrieved);
    }

    // listGames Tests
    @Test
    @Order(5)
    @DisplayName("List Games - Positive")
    public void listGamesPositive() throws DataAccessException {
        // Create multiple games
        gameDAO.createGame("Game 1");
        gameDAO.createGame("Game 2");
        gameDAO.createGame("Game 3");

        Collection<GameData> games = gameDAO.listGames();
        assertNotNull(games);
        assertEquals(3, games.size());
    }

    @Test
    @Order(6)
    @DisplayName("List Games - Negative (Empty List)")
    public void listGamesNegative() throws DataAccessException {
        // List games when table is empty
        Collection<GameData> games = gameDAO.listGames();
        assertNotNull(games);
        assertTrue(games.isEmpty());
    }

    // updateGame Tests
    @Test
    @Order(7)
    @DisplayName("Update Game - Positive")
    public void updateGamePositive() throws DataAccessException {
        int gameID = gameDAO.createGame("Update Test");
        GameData original = gameDAO.getGame(gameID);

        // Update game with players
        ChessGame updatedChessGame = new ChessGame();
        updatedChessGame.getBoard().resetBoard();
        GameData updated = new GameData(gameID, "whitePlayer", "blackPlayer",
                "Update Test", updatedChessGame);

        assertDoesNotThrow(() -> gameDAO.updateGame(updated));

        // Verify update
        GameData retrieved = gameDAO.getGame(gameID);
        assertNotNull(retrieved);
        assertEquals("whitePlayer", retrieved.whiteUsername());
        assertEquals("blackPlayer", retrieved.blackUsername());
        assertEquals("Update Test", retrieved.gameName());
    }

    @Test
    @Order(8)
    @DisplayName("Update Game - Negative (Non-existent Game)")
    public void updateGameNegative() {
        ChessGame game = new ChessGame();
        GameData nonExistent = new GameData(99999, "white", "black", "Fake Game", game);

        // Updating non-existent game should not throw exception but won't update anything
        assertDoesNotThrow(() -> gameDAO.updateGame(nonExistent));

        // Verify game still doesn't exist
        assertThrows(DataAccessException.class, () -> {
            GameData retrieved = gameDAO.getGame(99999);
            if (retrieved == null) {
                throw new DataAccessException("Game not found");
            }
        });
    }


}