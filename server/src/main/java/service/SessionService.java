package service;

import model.*;
import dataaccess.*;
import org.eclipse.jetty.server.Authentication;
import java.util.Map;
import java.util.Collection;
import java.util.UUID;
import io.javalin.http.Context;

public class SessionService {
    private final AuthDAO authDAO;
    private final UserDAO userDAO;
    private final GameDAO gameDAO;

    public SessionService(AuthDAO authDAO, UserDAO userDAO, GameDAO gameDAO){
        this.authDAO = authDAO;
        this.userDAO = userDAO;
        this.gameDAO = gameDAO;
    }

    public LoginSessionResult login(String username, String password) throws DataAccessException {
        if(username == null || password == null){
            throw new DataAccessException("Error: bad request");
        }
        //Get user information
        UserData user = userDAO.getUser(username);
        //Checks for errors
        if(user == null){
            throw new DataAccessException("Error: unauthorized");
        }
        if(!user.password().equals(password)){
            throw new DataAccessException("Error: unauthorized");
        }
        String authToken = UUID.randomUUID().toString();
        AuthData authData = new AuthData(authToken, username);
        authDAO.createAuth(authData);


        return new LoginSessionResult(username, authToken);
    }

    public void logout(String authToken) throws DataAccessException {

        if(authToken == null){
            throw new DataAccessException("Error: bad request");
        }
        AuthData authData = authDAO.getAuth(authToken);
        if(authData == null){
            throw new DataAccessException("Error: unauthorized");
        }
        authDAO.deleteAuth(authToken);
    }

}
