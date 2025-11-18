package service;

import model.*;
import dataaccess.*;
import java.util.Collection;

public class GameService {
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;

    public GameService(AuthDAO authDAO, GameDAO gameDAO){
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    public CreateGameResult createGame(String gameName, String authToken)throws DataAccessException{
        AuthData authData = authDAO.getAuth(authToken);
        if(authData == null){
            throw new DataAccessException("Error: unauthorized");
        }
        if(gameName == null){
            throw new DataAccessException("Error: bad request");
        }

        int gameID = gameDAO.createGame(gameName);

        return new CreateGameResult(gameID);
    }

    public void joinGame(Integer gameID, String playerColor, String authToken) throws DataAccessException {
        AuthData authData = authDAO.getAuth(authToken);

        if (authData == null) {
            throw new DataAccessException("Error: unauthorized");
        }
        if (gameID == null) {
            throw new DataAccessException("Error: bad request");
        }

        GameData game = gameDAO.getGame(gameID);
        if (game == null) {
            throw new DataAccessException("Error: bad request");
        }

        // Validate playerColor - must be "WHITE" or "BLACK" (required field)
        if (playerColor == null || playerColor.isEmpty() || 
            (!playerColor.equals("WHITE") && !playerColor.equals("BLACK"))) {
            throw new DataAccessException("Error: bad request");
        }

        // Join as a player
        String username = authData.username();
        if (playerColor.equals("WHITE")) {
            if (game.whiteUsername() != null) {
                throw new DataAccessException("Error: already taken");
            }
            GameData updatedGame = new GameData(game.gameID(), username, game.blackUsername(), game.gameName(), game.game());
            gameDAO.updateGame(updatedGame);
        } else {
            if (game.blackUsername() != null) {
                throw new DataAccessException("Error: already taken");
            }
            GameData updatedGame = new GameData(game.gameID(), game.whiteUsername(), username, game.gameName(), game.game());
            gameDAO.updateGame(updatedGame);
        }
    }

    public ListGamesResult listGames(String authToken)throws DataAccessException{
        AuthData authData = authDAO.getAuth(authToken);
        if(authData == null){
            throw new DataAccessException("Error: unauthorized");
        }
        Collection<GameData> games = gameDAO.listGames();
        return new ListGamesResult(games);
    }


}
