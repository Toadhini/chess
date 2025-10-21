package server.handlers;

import com.google.gson.Gson;
import io.javalin.http.Context;
import service.ClearService;
import model.ResponseMessage;
import dataaccess.DataAccessException;

public class ClearHandler {
    private final ClearService clearService;
    private final Gson gson = new Gson();

    public ClearHandler(ClearService clearService) {
        this.clearService = clearService;
    }

    public void handle(Context ctx) {
        try {
            clearService.clear();
            ctx.status(200);
            ctx.result(gson.toJson(new ResponseMessage("")));
            ctx.contentType("application/json");
        } catch (DataAccessException e) {
            ctx.status(500);
            ctx.result(gson.toJson(new ResponseMessage("Error: " + e.getMessage())));
            ctx.contentType("application/json");
        }
    }
}