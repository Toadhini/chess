package server;

import io.javalin.*;
import dataaccess.*;
import model.*;
import org.eclipse.jetty.server.Authentication;
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
        userService = new UserService(userDAO, authDAO);
        //Handler initialization
        clearHandler = new ClearHandler(clearService);
        // Register your endpoints and exception handlers here.
        registerEndpoints();
    }
    private void registerEndpoints() {
        // Clear endpoint
        javalin.delete("/db", ctx -> {
            try {
                clearService.clear();
                ctx.status(200);
                ctx.json(new ResponseMessage(""));
            } catch (DataAccessException e) {
                ctx.status(500);
                ctx.json(new ResponseMessage("Error: " + e.getMessage()));
            }
        });

        // Register endpoint
        javalin.post("/user", ctx -> {
            try {
                UserData userData = ctx.bodyAsClass(UserData.class);
                RegisterResult result = userService.register(userData);
                ctx.status(200);
                ctx.json(result);
            } catch (DataAccessException e) {
                if (e.getMessage().contains("bad request")) {
                    ctx.status(400);
                } else if (e.getMessage().contains("already taken")) {
                    ctx.status(403);
                } else {
                    ctx.status(500);
                }
                ctx.json(new RegisterResult(e.getMessage()));
            }
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
