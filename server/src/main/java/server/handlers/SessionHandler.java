package server.handlers;

import com.google.gson.Gson;
import service.SessionService;
import model.*;
import dataaccess.DataAccessException;
import io.javalin.http.Context;
import java.util.Map;

public class SessionHandler {
    private final SessionService sessionService;
    private final Gson gson = new Gson();

    public SessionHandler(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    public void login(Context ctx){
        try {
            // Extract request body
            LoginSessionRequest request = gson.fromJson(ctx.body(), LoginSessionRequest.class);

            // Call service
            LoginSessionResult result = sessionService.login(request.username(), request.password());

            // Send success response
            ctx.status(200);
            ctx.result(gson.toJson(result));
            ctx.contentType("application/json");

        } catch (DataAccessException e) {
            // Handle errors with appropriate status codes
            if (e.getMessage().contains("bad request")) {
                ctx.status(400);
            } else if (e.getMessage().contains("unauthorized")) {
                ctx.status(401);
            } else {
                ctx.status(500);
            }
            ctx.result(gson.toJson(new LoginSessionResult(e.getMessage())));
            ctx.contentType("application/json");
        }
    }
    public void logout(Context ctx){
        try {
            // Extract authorization header
            String authToken = ctx.header("authorization");

            // Call service
            sessionService.logout(authToken);

            // Send success response (empty JSON object)
            ctx.status(200);
            ctx.result(gson.toJson(Map.of()));
            ctx.contentType("application/json");

        } catch (DataAccessException e) {
            // Handle errors with appropriate status codes
            if (e.getMessage().contains("unauthorized")) {
                ctx.status(401);
            } else {
                ctx.status(500);
            }
            ctx.result(gson.toJson(new LoginSessionResult(e.getMessage())));
            ctx.contentType("application/json");
        }
    }

}
