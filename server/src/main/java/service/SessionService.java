package service;

import model.*;
import dataaccess.*;
import org.eclipse.jetty.server.Authentication;

import java.util.Collection;


public class SessionService {
    private final AuthDAO authDAO;
    private final UserDAO userDAO;
    private final GameDAO gameDAO;

    public SessionService(AuthDAO authDAO, UserDAO userDAO, GameDAO gameDAO){
        this.authDAO = authDAO;
        this.userDAO = userDAO;
        this.gameDAO = gameDAO;
    }

    public void login(String userName, String authToken) throws DataAccessException {
        AuthData authData = authDAO.getAuth(authToken);

    }


}
