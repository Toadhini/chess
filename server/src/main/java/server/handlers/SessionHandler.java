package server.handlers;

import service.SessionService;
import model.*;
import dataaccess.DataAccessException;
import io.javalin.http.Context;

public class SessionHandler {
    private final SessionService sessionService;

    public SessionHandler(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    public void login(Context ctx){
        try {
            // Extract request body
            LoginSessionRequest request = ctx.bodyAsClass(LoginSessionRequest.class);

            // Call service
            LoginSessionResult result = sessionService.login(request.username(), request.password());

            // Send success response
            ctx.status(200);
            ctx.json(result);

        } catch (DataAccessException e) {
            // Handle errors with appropriate status codes
            if (e.getMessage().contains("bad request")) {
                ctx.status(400);
            } else if (e.getMessage().contains("unauthorized")) {
                ctx.status(401);
            } else {
                ctx.status(500);
            }
            ctx.json(new LoginSessionResult(e.getMessage()));
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
            ctx.json(new Object());

        } catch (DataAccessException e) {
            // Handle errors with appropriate status codes
            if (e.getMessage().contains("unauthorized")) {
                ctx.status(401);
            } else {
                ctx.status(500);
            }
            ctx.json(new LoginSessionResult(e.getMessage()));
        }
    }

}
