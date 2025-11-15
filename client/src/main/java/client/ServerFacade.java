package client;

import com.google.gson.Gson;
import model.*;
import chess.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.*;

public class ServerFacade {
    private final String serverUrl;
    private final Gson gson = new Gson();

    public ServerFacade(int port){
        this.serverUrl = "http://localhost:" + port;
    }

    //Register function for users to register within client

    public AuthData register(String username, String password, String email) throws Exception{
        UserData userData = new UserData(username, password, email);
        RegisterResult registerResult = makeRequest("POST", "/user", userData, RegisterResult.class, null);

        if(registerResult.message() != null){
            throw new Exception(registerResult.message());
        }
        return new AuthData(registerResult.authToken(), registerResult.username());
    }

    //Login function for users to login within client

    public AuthData login(String username, String password) throws Exception{
        LoginSessionRequest request = new LoginSessionRequest(username, password);
        LoginSessionResult result = makeRequest("POST", "/session", request, LoginSessionResult.class, null);

        if (result.message() != null){
            throw new Exception(result.message());
        }

        return new AuthData(result.authToken(), result.username());
    }

    //Logout function for users to logout of client

    public void logout(String authToken) throws Exception{
        makeRequest("DELETE", "/session", null, null, authToken);
    }

    //List game function

    public ListGamesResult listGames(String authToken)throws Exception{
        return makeRequest("GET", "/game", null,ListGamesResult.class, authToken);
    }


     //Helper Method for making HTTP requests to the server


    private <T> T makeRequest(String method, String path, Object requestBody, Class<T> responseClass, String authToken) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create(serverUrl + path);


    }
}
