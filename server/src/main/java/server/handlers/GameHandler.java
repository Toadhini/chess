package server.handlers;

import com.google.gson.Gson;
import service.GameService;
import model.*;
import dataaccess.DataAccessException;
import io.javalin.http.Context;

public class GameHandler {
    private final GameService gameService;
    private final Gson gson = new Gson();

    public GameHandler(GameService gameService){
        this.gameService = gameService;
    }

    public void createGame(Context ctx){
        try {
            String authToken = ctx.header("authorization");


            CreateGameRequest request = gson.fromJson(ctx.body(), CreateGameRequest.class);

            CreateGameResult result = gameService.createGame(request.gameName(), authToken);

            ctx.status(200);
            ctx.result(gson.toJson(result));
            ctx.contentType("application/json");

        } catch (DataAccessException e) {
            //Error handling
            if (e.getMessage().contains("bad request")) {
                ctx.status(400);
            } else if (e.getMessage().contains("unauthorized")) {
                ctx.status(401);
            } else {
                ctx.status(500);
            }
            ctx.result(gson.toJson(new CreateGameResult(e.getMessage())));
            ctx.contentType("application/json");
        }
    }
    public void joinGame(Context ctx) {
        try {
            // Get auth token from header
            String authToken = ctx.header("authorization");

            // Extract request body
            JoinGameRequest request = gson.fromJson(ctx.body(), JoinGameRequest.class);

            // Call service
            gameService.joinGame(request.gameID(), request.playerColor(), authToken);

            // Send success response
            ctx.status(200);
            ctx.result(gson.toJson(new ResponseMessage("")));
            ctx.contentType("application/json");

        } catch (DataAccessException e) {
            // Handle errors with appropriate status codes
            if (e.getMessage().contains("bad request")) {
                ctx.status(400);
            } else if (e.getMessage().contains("unauthorized")) {
                ctx.status(401);
            } else if (e.getMessage().contains("already taken")) {
                ctx.status(403);
            } else {
                ctx.status(500);
            }
            ctx.result(gson.toJson(new ResponseMessage(e.getMessage())));
            ctx.contentType("application/json");
        }
    }

    public void listGames(Context ctx) {
        try {
            // Get auth token from header
            String authToken = ctx.header("authorization");

            // Call service
            ListGamesResult result = gameService.listGames(authToken);

            // Send success response
            ctx.status(200);
            ctx.result(gson.toJson(result));
            ctx.contentType("application/json");

        } catch (DataAccessException e) {
            // Handle errors with appropriate status codes
            if (e.getMessage().contains("unauthorized")) {
                ctx.status(401);
            } else {
                ctx.status(500);
            }
            ctx.result(gson.toJson(new ListGamesResult(e.getMessage())));
            ctx.contentType("application/json");
        }
    }
}

