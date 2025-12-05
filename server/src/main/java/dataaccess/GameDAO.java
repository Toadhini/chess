package dataaccess;

import chess.ChessGame;
import model.GameData;
import java.util.Collection;

public interface GameDAO {
    int createGame(String gameName)throws DataAccessException;
    GameData getGame(int gameID)throws DataAccessException;
    Collection<GameData> listGames()throws DataAccessException;
    void updateGame(GameData game)throws DataAccessException;
    void clear()throws DataAccessException;
    void updateGame(int gameID, ChessGame game) throws DataAccessException;
}
