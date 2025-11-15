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

    public AuthData register(String username, String password, String email) throws Exception{

    }

     //Helper Method for making HTTP requests to the server


    private <T> T makeRequest(String method, String path, Object requestBody, Class<T> responseClass, String authToken) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create(serverUrl + path);


    }
}
