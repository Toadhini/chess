package server.handlers;

import io.javalin.http.Context;
import service.ClearService;
import model.ResponseMessage;

public class ClearHandler {
    private final ClearService clearService;

    public ClearHandler(ClearService clearService){
        this.clearService = clearService;
    }

    public void handle(Context ctx){
        try{
            clearService.clear();
            ctx.status(200);
            ctx.json(new ResponseMessage(""));
        }
        catch (Exception e){
            ctx.status(500);
            ctx.json(new ResponseMessage("Error: " + e.getMessage()));
        }
    }
}
