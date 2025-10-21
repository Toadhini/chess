package service;

import dataaccess.*;
import model.*;
import java.util.UUID;

public class UserService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public UserService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public RegisterResult register(UserData userData) throws DataAccessException {
        //Validate input
        if (userData.username() == null || userData.password() == null || userData.email() == null) {
            throw new DataAccessException("Error: bad request");
        }

        //Check if user already exists
        if (userDAO.getUser(userData.username()) != null) {
            throw new DataAccessException("Error: already taken");
        }

        //Create the user
        userDAO.createUser(userData);

        //Generate auth token
        String authToken = UUID.randomUUID().toString();
        AuthData authData = new AuthData(authToken, userData.username());
        authDAO.createAuth(authData);

        //Return success result
        return new RegisterResult(userData.username(), authToken);
    }
}