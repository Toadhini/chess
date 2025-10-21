package server.handlers;

import com.google.gson.Gson;
import io.javalin.http.Context;
import service.UserService;
import model.*;
import dataaccess.DataAccessException;

public class UserHandler {
    private final UserService userService;
    private final Gson gson = new Gson();

    public UserHandler(UserService userService) {
        this.userService = userService;
    }

    public void register(Context ctx) {
        try {
            // Extract request body
            UserData userData = gson.fromJson(ctx.body(), UserData.class);

            // Call service
            RegisterResult result = userService.register(userData);

            // Send success response
            ctx.status(200);
            ctx.result(gson.toJson(result));
            ctx.contentType("application/json");

        } catch (DataAccessException e) {
            // Handle errors with appropriate status codes
            if (e.getMessage().contains("bad request")) {
                ctx.status(400);
            } else if (e.getMessage().contains("already taken")) {
                ctx.status(403);
            } else {
                ctx.status(500);
            }
            ctx.result(gson.toJson(new RegisterResult(e.getMessage())));
            ctx.contentType("application/json");
        }
    }
}