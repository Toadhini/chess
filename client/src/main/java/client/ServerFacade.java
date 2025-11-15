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

    //Create a game function

    public CreateGameResult createGame(String authToken, String gameName)throws Exception{
        CreateGameRequest request = new CreateGameRequest(gameName);
        CreateGameResult result = makeRequest("POST", "/game", request, CreateGameResult.class, authToken);

        if(result.message() != null){
            throw new Exception(result.message());
        }

        return result;
    }

    //Join game function

    public void joinGame(String authToken, int gameID, String playerColer)throws Exception{
        JoinGameRequest request = new JoinGameRequest(playerColer, gameID);
        makeRequest("PUT", "/game", request, null, authToken);
    }

     //Helper Method for making HTTP requests to the server

    private <T> T makeRequest(String method, String path, Object requestBody, Class<T> responseClass, String authToken) throws Exception {
        try {
            //Create HTTP client
            HttpClient client = HttpClient.newHttpClient();
            URI uri = URI.create(serverUrl + path);

            //Build request
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Content-Type", "application/json");

            //Add authorization header if provided
            if (authToken != null) {
                requestBuilder.header("authorization", authToken);
            }

            //Set method and body
            if (method.equals("GET")) {
                requestBuilder.GET();
            } else if (method.equals("DELETE")) {
                requestBuilder.DELETE();
            } else if (method.equals("POST")) {
                String bodyJson = requestBody != null ? gson.toJson(requestBody) : "";
                requestBuilder.POST(HttpRequest.BodyPublishers.ofString(bodyJson));
            } else if (method.equals("PUT")) {
                String bodyJson = requestBody != null ? gson.toJson(requestBody) : "";
                requestBuilder.PUT(HttpRequest.BodyPublishers.ofString(bodyJson));
            }

            HttpRequest request = requestBuilder.build();

            //Send request and get response
            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

            //Check status code
            int statusCode = response.statusCode();

            //Handle error status codes
            if (statusCode != 200) {
                //Try to parse error message from response
                try (InputStream respBody = response.body()) {
                    InputStreamReader reader = new InputStreamReader(respBody);
                    ResponseMessage errorMsg = gson.fromJson(reader, ResponseMessage.class);
                    String message = errorMsg != null && errorMsg.getMessage() != null
                            ? errorMsg.getMessage()
                            : "Request failed with status code: " + statusCode;
                    throw new Exception(message);
                }
            }

            //Parse successful response
            if (responseClass != null) {
                try (InputStream respBody = response.body()) {
                    InputStreamReader reader = new InputStreamReader(respBody);
                    return gson.fromJson(reader, responseClass);
                }
            }

            return null;

        } catch (IOException | InterruptedException e) {
            throw new Exception("Network error: " + e.getMessage());
        }
    }
}
