package server;

import io.javalin.*;
import dataaccess.*;
import model.ResponseMessage;
import service.*;
import server.handlers.*;

public class Server {

    private final Javalin javalin;

    //DAOs
    private final UserDAO userDAO;
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;

    //services
    private final ClearService clearService;

    //Handlers
    private final ClearHandler clearHandler;

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        //DAO initialization
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        gameDAO = new MemoryGameDAO();
        //Service initialization
        clearService = new ClearService(userDAO, authDAO, gameDAO);
        //Handler initialization
        clearHandler = new ClearHandler(clearService);
        // Register your endpoints and exception handlers here.
        // Register your endpoints and exception handlers here.
        javalin.delete("/db", clearHandler::handle);
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
