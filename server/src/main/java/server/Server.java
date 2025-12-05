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
    private final SessionService sessionService;

    //Websocket parts
    private final ConnectionManager connectionManager;
    private final WebSocketHandler webSocketHandler;

    //Handlers
    private final ClearHandler clearHandler;
    private final UserHandler userHandler;
    private final GameHandler gameHandler;
    private final SessionHandler sessionHandler;

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        //DAO initialization
        try {
            userDAO = new MySQLUserDAO();
            authDAO = new MySQLAuthDAO();
            gameDAO = new MySQLGameDAO();
        } catch (DataAccessException e) {
            throw new RuntimeException("Unable to initialize database: " + e.getMessage());
        }
        //Service initialization
        clearService = new ClearService(userDAO, authDAO, gameDAO);
        userService = new UserService(userDAO, authDAO);
        gameService = new GameService(authDAO, gameDAO);
        sessionService = new SessionService(authDAO, userDAO, gameDAO);
        //Websocket initialization
        connectionManager = new ConnectionManager();
        webSocketHandler = new WebSocketHandler(connectionManager, authDAO, gameDAO);
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

        javalin.ws("/ws", ws -> {
            ws.onConnect(webSocketHandler::onConnect);
            ws.onMessage(webSocketHandler::onMessage);
            ws.onClose(webSocketHandler::onClose);
            ws.onError(webSocketHandler::onError);
        });
    }


    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}