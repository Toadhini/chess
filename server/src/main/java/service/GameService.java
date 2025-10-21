package service;

import model.*;
import dataaccess.*;
import java.util.Collection;
import chess.ChessGame;

public class GameService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;

    public GameService(AuthDAO authDAO, UserDAO userDAO, GameDAO gameDAO){
        this.userDAO = userDAO;
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

    public void JoinGame(Integer gameID, String playerColor, String authToken)throws DataAccessException{
        AuthData authData = authDAO.getAuth(authToken);

        if(authToken == null){
            throw new DataAccessException("Error: unauthorized");
        }
        if(gameID == null || playerColor == null){
            throw new DataAccessException("Error: bad request");
        }
        if (!playerColor.equals("WHITE") && !playerColor.equals("BLACK")) {
            throw new DataAccessException("Error: bad request");
        }

        GameData game = gameDAO.getGame(gameID);
        if(game == null){
            throw new DataAccessException("Error: bad request");
        }

        String username = authData.username();
        if(playerColor.equals("White")){
            if(game.whiteUsername() != null){
                throw new DataAccessException("Error: already taken");
            }
            GameData updatedGame = new GameData(game.gameID(), username, game.blackUsername(), game.gameName(), game.game());
            gameDAO.updateGame(updatedGame);
        }
        else{
            if(game.blackUsername() != null){
                throw new DataAccessException("Error: already taken");
            }
            GameData updatedGame = new GameData(game.gameID(), username, game.whiteUsername(), game.gameName(), game.game());
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
