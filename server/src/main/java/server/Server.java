package server;

import io.javalin.*;
import dataaccess.*;
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
    private final UserService userService;
    private final GameService gameService;

    //Handlers
    private final ClearHandler clearHandler;
    private final UserHandler userHandler;
    private final GameHandler gameHandler;

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        //DAO initialization
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        gameDAO = new MemoryGameDAO();
        //Service initialization
        clearService = new ClearService(userDAO, authDAO, gameDAO);
        userService = new UserService(userDAO, authDAO);
        gameService = new GameService(authDAO, gameDAO);
        //Handler initialization
        clearHandler = new ClearHandler(clearService);
        userHandler = new UserHandler(userService);
        gameHandler = new GameHandler(gameService);
        // Register your endpoints and exception handlers here.
        javalin.delete("/db", clearHandler::handle);
        javalin.post("/user", userHandler::register);
        javalin.get("/game", gameHandler::listGames);
        javalin.post("/game", gameHandler::createGame);
        javalin.put("/game", gameHandler::joinGame);
    }


    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
