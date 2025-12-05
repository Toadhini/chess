package dataaccess;

import chess.ChessGame;
import model.GameData;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MemoryGameDAO implements GameDAO {
    private final Map<Integer, GameData> games = new HashMap<>();
    private int nextGameID = 1;

    @Override
    public int createGame(String gameName) throws DataAccessException {
        int gameID = nextGameID++;
        ChessGame game = new ChessGame();
        GameData gameData = new GameData(gameID, null, null, gameName, game);
        games.put(gameID, gameData);
        return gameID;
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        return games.get(gameID);
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        return games.values();
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        games.put(game.gameID(), game);
    }

    @Override
    public void updateGame(int gameID, ChessGame game) throws DataAccessException {
        GameData existingGame = games.get(gameID);
        if (existingGame != null) {
            GameData updatedGame = new GameData(
                existingGame.gameID(),
                existingGame.whiteUsername(),
                existingGame.blackUsername(),
                existingGame.gameName(),
                game
            );
            games.put(gameID, updatedGame);
        }
    }

    @Override
    public void clear() throws DataAccessException {
        games.clear();
        nextGameID = 1;
    }
}