package server;

import io.javalin.*;
import dataaccess.*;
import service.*;
import server.handlers.*;

public class Server {
    static {
        // Force Jackson to be loaded before Javalin initializes
        try {
            Class.forName("com.fasterxml.jackson.databind.ObjectMapper");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Jackson not found", e);
        }
    }

    private final Javalin javalin;

    //DAOs
    private final UserDAO userDAO;
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;

    //services
    private final ClearService clearService;
    private final UserService userService;
    private final GameService gameService;
    private final SessionService sessionService;

    //Handlers
    private final ClearHandler clearHandler;
    private final UserHandler userHandler;
    private final GameHandler gameHandler;
    private final SessionHandler sessionHandler;

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
        sessionService = new SessionService(authDAO, userDAO, gameDAO);

        //Handler initialization
        clearHandler = new ClearHandler(clearService);
        userHandler = new UserHandler(userService);
        gameHandler = new GameHandler(gameService);
        sessionHandler = new SessionHandler(sessionService);
        // Register your endpoints and exception handlers here.
        javalin.delete("/db", clearHandler::handle);
        javalin.post("/user", userHandler::register);
        javalin.get("/game", gameHandler::listGames);
        javalin.post("/game", gameHandler::createGame);
        javalin.put("/game", gameHandler::joinGame);
        javalin.post("/session", sessionHandler::login);
        javalin.delete("/session", sessionHandler::logout);
    }


    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}