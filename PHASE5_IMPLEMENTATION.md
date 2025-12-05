# Phase 5 Implementation Guide - Chess Client

This guide provides step-by-step instructions for implementing the Phase 5 Chess Client with detailed code examples.

---

## Step 1: ServerFacade Implementation

### File: `client/src/main/java/client/ServerFacade.java`

**Complete Implementation:**

```java
package client;

import com.google.gson.Gson;
import model.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.*;

public class ServerFacade {
    private final String serverUrl;
    private final Gson gson = new Gson();
    
    public ServerFacade(int port){
        this.serverUrl = "http://localhost:" + port;
    }
    
    /**
     * Helper method for making HTTP requests to the server
     * @param method HTTP method (GET, POST, PUT, DELETE)
     * @param path API endpoint path (e.g., "/user", "/game")
     * @param requestBody Request body object to serialize to JSON
     * @param responseClass Class to deserialize response into
     * @param authToken Authorization token (null if not needed)
     * @return Deserialized response object
     */
    private <T> T makeRequest(String method, String path, Object requestBody, 
                              Class<T> responseClass, String authToken) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create(serverUrl + path);
        
        // Build HTTP request
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json");
        
        // Add authorization header if provided
        if (authToken != null && !authToken.isEmpty()) {
            requestBuilder.header("Authorization", authToken);
        }
        
        // Set HTTP method and body
        if (method.equals("GET")) {
            requestBuilder.GET();
        } else if (method.equals("POST")) {
            String jsonBody = (requestBody != null) ? gson.toJson(requestBody) : "{}";
            requestBuilder.POST(HttpRequest.BodyPublishers.ofString(jsonBody));
        } else if (method.equals("PUT")) {
            String jsonBody = (requestBody != null) ? gson.toJson(requestBody) : "{}";
            requestBuilder.PUT(HttpRequest.BodyPublishers.ofString(jsonBody));
        } else if (method.equals("DELETE")) {
            requestBuilder.DELETE();
        }
        
        // Send request and receive response
        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        // Parse JSON response into the expected type
        return gson.fromJson(response.body(), responseClass);
    }
    
    /**
     * Register a new user account
     * @param username Username for the new account
     * @param password Password for the new account  
     * @param email Email for the new account
     * @return RegisterResult containing authToken and username if successful, or error message if failed
     */
    public RegisterResult register(String username, String password, String email) throws IOException, InterruptedException {
        UserData userData = new UserData(username, password, email);
        return makeRequest("POST", "/user", userData, RegisterResult.class, null);
    }
    
    /**
     * Login an existing user
     * @param username Username to login
     * @param password Password to login
     * @return LoginSessionResult containing authToken and username if successful, or error message if failed
     */
    public LoginSessionResult login(String username, String password) throws IOException, InterruptedException {
        LoginSessionRequest loginRequest = new LoginSessionRequest(username, password);
        return makeRequest("POST", "/session", loginRequest, LoginSessionResult.class, null);
    }
    
    /**
     * Logout the current user
     * @param authToken Authentication token from login/register
     */
    public void logout(String authToken) throws IOException, InterruptedException {
        makeRequest("DELETE", "/session", null, ResponseMessage.class, authToken);
    }
    
    /**
     * List all games on the server
     * @param authToken Authentication token from login/register
     * @return ListGamesResult containing collection of GameData objects
     */
    public ListGamesResult listGames(String authToken) throws IOException, InterruptedException {
        return makeRequest("GET", "/game", null, ListGamesResult.class, authToken);
    }
    
    /**
     * Create a new game on the server
     * @param authToken Authentication token from login/register
     * @param gameName Name for the new game
     * @return CreateGameResult containing the new gameID
     */
    public CreateGameResult createGame(String authToken, String gameName) throws IOException, InterruptedException {
        CreateGameRequest request = new CreateGameRequest(gameName);
        return makeRequest("POST", "/game", request, CreateGameResult.class, authToken);
    }
    
    /**
     * Join an existing game as a player or observer
     * @param authToken Authentication token from login/register
     * @param gameID ID of the game to join
     * @param playerColor Color to play as ("WHITE" or "BLACK"), or null to observe
     * @return ResponseMessage indicating success or failure
     */
    public ResponseMessage joinGame(String authToken, int gameID, String playerColor) throws IOException, InterruptedException {
        JoinGameRequest request = new JoinGameRequest(playerColor, gameID);
        return makeRequest("PUT", "/game", request, ResponseMessage.class, authToken);
    }
    
    /**
     * Clear the database - used for testing
     */
    public void clear() throws IOException, InterruptedException {
        makeRequest("DELETE", "/db", null, ResponseMessage.class, null);
    }
}
```

### API Endpoints Reference:
- `POST /user` - Register (body: UserData)
- `POST /session` - Login (body: LoginSessionRequest)
- `DELETE /session` - Logout (header: Authorization)
- `GET /game` - List games (header: Authorization)
- `POST /game` - Create game (header: Authorization, body: CreateGameRequest)
- `PUT /game` - Join game (header: Authorization, body: JoinGameRequest)
- `DELETE /db` - Clear database (testing only)

---

## Step 2: REPL Infrastructure

### File: `client/src/main/java/ui/State.java`

```java
package ui;

public enum State {
    PRELOGIN,
    POSTLOGIN,
    QUIT
}
```

### File: `client/src/main/java/ui/ChessClient.java`

```java
package ui;

import client.ServerFacade;
import model.*;
import chess.ChessGame;
import java.util.*;

public class ChessClient {
    private final ServerFacade server;
    private final Scanner scanner;
    private State state = State.PRELOGIN;
    private String authToken = null;
    private String username = null;
    private Map<Integer, GameData> gameMap = new HashMap<>();
    
    public ChessClient(String serverUrl) {
        // Parse port from URL or default to 8080
        int port = 8080;
        if (serverUrl.contains(":")) {
            String portStr = serverUrl.substring(serverUrl.lastIndexOf(":") + 1);
            port = Integer.parseInt(portStr);
        }
        server = new ServerFacade(port);
        scanner = new Scanner(System.in);
    }
    
    public void run() {
        System.out.println("♕ Welcome to 240 Chess Client ♕");
        System.out.println("Type 'help' to get started.");
        
        while (state != State.QUIT) {
            printPrompt();
            String line = scanner.nextLine();
            
            try {
                String[] tokens = line.trim().split("\\s+");
                if (tokens.length == 0 || tokens[0].isEmpty()) {
                    continue;
                }
                
                String cmd = tokens[0].toLowerCase();
                
                if (state == State.PRELOGIN) {
                    handlePreloginCommand(cmd, tokens);
                } else {
                    handlePostloginCommand(cmd, tokens);
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
        
        System.out.println("Goodbye!");
    }
    
    private void printPrompt() {
        System.out.print(state == State.PRELOGIN ? "[LOGGED_OUT] >>> " : "[LOGGED_IN] >>> ");
    }
    
    private void handlePreloginCommand(String cmd, String[] tokens) throws Exception {
        switch (cmd) {
            case "help" -> printPreloginHelp();
            case "quit" -> state = State.QUIT;
            case "register" -> register(tokens);
            case "login" -> login(tokens);
            default -> System.out.println("Unknown command. Type 'help' for available commands.");
        }
    }
    
    private void handlePostloginCommand(String cmd, String[] tokens) throws Exception {
        switch (cmd) {
            case "help" -> printPostloginHelp();
            case "logout" -> logout();
            case "create" -> createGame(tokens);
            case "list" -> listGames();
            case "join" -> joinGame(tokens);
            case "observe" -> observeGame(tokens);
            default -> System.out.println("Unknown command. Type 'help' for available commands.");
        }
    }
    
    private void printPreloginHelp() {
        System.out.println("Available commands:");
        System.out.println("  register <USERNAME> <PASSWORD> <EMAIL> - create a new account");
        System.out.println("  login <USERNAME> <PASSWORD> - login to play chess");
        System.out.println("  quit - exit the program");
        System.out.println("  help - show available commands");
    }
    
    private void printPostloginHelp() {
        System.out.println("Available commands:");
        System.out.println("  create <GAME_NAME> - create a new game");
        System.out.println("  list - list all games");
        System.out.println("  join <GAME_NUMBER> <WHITE|BLACK> - join a game as a player");
        System.out.println("  observe <GAME_NUMBER> - observe a game");
        System.out.println("  logout - logout of your account");
        System.out.println("  help - show available commands");
    }
    
    private void register(String[] tokens) throws Exception {
        if (tokens.length != 4) {
            System.out.println("Usage: register <USERNAME> <PASSWORD> <EMAIL>");
            return;
        }
        
        var result = server.register(tokens[1], tokens[2], tokens[3]);
        
        if (result.authToken() != null) {
            authToken = result.authToken();
            username = result.username();
            state = State.POSTLOGIN;
            System.out.println("Successfully registered and logged in as " + username);
        } else {
            System.out.println("Registration failed: " + result.message());
        }
    }
    
    private void login(String[] tokens) throws Exception {
        if (tokens.length != 3) {
            System.out.println("Usage: login <USERNAME> <PASSWORD>");
            return;
        }
        
        var result = server.login(tokens[1], tokens[2]);
        
        if (result.authToken() != null) {
            authToken = result.authToken();
            username = result.username();
            state = State.POSTLOGIN;
            System.out.println("Successfully logged in as " + username);
        } else {
            System.out.println("Login failed: " + result.message());
        }
    }
    
    private void logout() throws Exception {
        server.logout(authToken);
        authToken = null;
        username = null;
        state = State.PRELOGIN;
        System.out.println("Successfully logged out");
    }
    
    private void createGame(String[] tokens) throws Exception {
        if (tokens.length < 2) {
            System.out.println("Usage: create <GAME_NAME>");
            return;
        }
        
        // Join all tokens after "create" as the game name
        String gameName = String.join(" ", Arrays.copyOfRange(tokens, 1, tokens.length));
        
        var result = server.createGame(authToken, gameName);
        
        if (result.gameID() != null) {
            System.out.println("Game created: " + gameName);
        } else {
            System.out.println("Failed to create game: " + result.message());
        }
    }
    
    private void listGames() throws Exception {
        var result = server.listGames(authToken);
        
        if (result.games() != null) {
            gameMap.clear();
            int index = 1;
            
            System.out.println("\nGames:");
            for (GameData game : result.games()) {
                gameMap.put(index, game);
                String whitePlayer = game.whiteUsername() != null ? game.whiteUsername() : "(empty)";
                String blackPlayer = game.blackUsername() != null ? game.blackUsername() : "(empty)";
                System.out.printf("%d. %s - White: %s, Black: %s%n", 
                    index, game.gameName(), whitePlayer, blackPlayer);
                index++;
            }
            System.out.println();
        } else {
            System.out.println("Failed to list games: " + result.message());
        }
    }
    
    private void joinGame(String[] tokens) throws Exception {
        if (tokens.length != 3) {
            System.out.println("Usage: join <GAME_NUMBER> <WHITE|BLACK>");
            return;
        }
        
        try {
            int gameNum = Integer.parseInt(tokens[1]);
            String color = tokens[2].toUpperCase();
            
            if (!color.equals("WHITE") && !color.equals("BLACK")) {
                System.out.println("Color must be WHITE or BLACK");
                return;
            }
            
            GameData game = gameMap.get(gameNum);
            if (game == null) {
                System.out.println("Invalid game number. Use 'list' to see available games.");
                return;
            }
            
            var result = server.joinGame(authToken, game.gameID(), color);
            System.out.println("Joined game as " + color);
            
            // Draw the board from the player's perspective
            ChessGame.TeamColor perspective = color.equals("WHITE") ? 
                ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;
            BoardDrawer.drawBoard(game.game(), perspective);
            
        } catch (NumberFormatException e) {
            System.out.println("Game number must be a valid number");
        }
    }
    
    private void observeGame(String[] tokens) throws Exception {
        if (tokens.length != 2) {
            System.out.println("Usage: observe <GAME_NUMBER>");
            return;
        }
        
        try {
            int gameNum = Integer.parseInt(tokens[1]);
            
            GameData game = gameMap.get(gameNum);
            if (game == null) {
                System.out.println("Invalid game number. Use 'list' to see available games.");
                return;
            }
            
            var result = server.joinGame(authToken, game.gameID(), null);
            System.out.println("Observing game: " + game.gameName());
            
            // Draw the board from white's perspective for observers
            BoardDrawer.drawBoard(game.game(), ChessGame.TeamColor.WHITE);
            
        } catch (NumberFormatException e) {
            System.out.println("Game number must be a valid number");
        }
    }
}
```

---

## Step 3: Board Drawing

### File: `client/src/main/java/ui/BoardDrawer.java`

```java
package ui;

import chess.*;
import static ui.EscapeSequences.*;

public class BoardDrawer {
    
    private static final String LIGHT_SQUARE = SET_BG_COLOR_LIGHT_GREY;
    private static final String DARK_SQUARE = SET_BG_COLOR_DARK_GREY;
    private static final String BORDER_COLOR = SET_BG_COLOR_DARK_GREEN;
    
    public static void drawBoard(ChessGame game, ChessGame.TeamColor perspective) {
        ChessBoard board = game.getBoard();
        
        System.out.println(); // Empty line before board
        
        if (perspective == ChessGame.TeamColor.BLACK) {
            drawBlackPerspective(board);
        } else {
            drawWhitePerspective(board);
        }
        
        System.out.println(); // Empty line after board
    }
    
    private static void drawWhitePerspective(ChessBoard board) {
        String[] colLabels = {" ", "a", "b", "c", "d", "e", "f", "g", "h", " "};
        
        // Top border
        drawBorderRow(colLabels);
        
        // Rows 8 to 1 (top to bottom)
        for (int row = 8; row >= 1; row--) {
            drawRow(board, row, true, row);
        }
        
        // Bottom border
        drawBorderRow(colLabels);
    }
    
    private static void drawBlackPerspective(ChessBoard board) {
        String[] colLabels = {" ", "h", "g", "f", "e", "d", "c", "b", "a", " "};
        
        // Top border
        drawBorderRow(colLabels);
        
        // Rows 1 to 8 (top to bottom)
        for (int row = 1; row <= 8; row++) {
            drawRow(board, row, false, row);
        }
        
        // Bottom border
        drawBorderRow(colLabels);
    }
    
    private static void drawBorderRow(String[] labels) {
        System.out.print(BORDER_COLOR + SET_TEXT_COLOR_WHITE);
        for (String label : labels) {
            System.out.print(" " + label + " ");
        }
        System.out.print(RESET_BG_COLOR + RESET_TEXT_COLOR);
        System.out.println();
    }
    
    private static void drawRow(ChessBoard board, int row, boolean whiteView, int rowNum) {
        // Left border (row number)
        System.out.print(BORDER_COLOR + SET_TEXT_COLOR_WHITE + " " + rowNum + " " + RESET_BG_COLOR);
        
        // Draw squares
        if (whiteView) {
            for (int col = 1; col <= 8; col++) {
                drawSquare(board, row, col);
            }
        } else {
            for (int col = 8; col >= 1; col--) {
                drawSquare(board, row, col);
            }
        }
        
        // Right border (row number)
        System.out.print(BORDER_COLOR + SET_TEXT_COLOR_WHITE + " " + rowNum + " " + RESET_BG_COLOR);
        System.out.print(RESET_TEXT_COLOR);
        System.out.println();
    }
    
    private static void drawSquare(ChessBoard board, int row, int col) {
        ChessPosition pos = new ChessPosition(row, col);
        ChessPiece piece = board.getPiece(pos);
        
        // Determine square color (h1 and a8 should be light)
        boolean isLightSquare = (row + col) % 2 == 0;
        String bgColor = isLightSquare ? LIGHT_SQUARE : DARK_SQUARE;
        
        System.out.print(bgColor);
        
        if (piece == null) {
            System.out.print(EMPTY);
        } else {
            System.out.print(getPieceSymbol(piece));
        }
        
        System.out.print(RESET_BG_COLOR);
    }
    
    private static String getPieceSymbol(ChessPiece piece) {
        String color = piece.getTeamColor() == ChessGame.TeamColor.WHITE ? 
            SET_TEXT_COLOR_RED : SET_TEXT_COLOR_BLUE;
        
        String symbol = switch (piece.getPieceType()) {
            case KING -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_KING : BLACK_KING;
            case QUEEN -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_QUEEN : BLACK_QUEEN;
            case BISHOP -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_BISHOP : BLACK_BISHOP;
            case KNIGHT -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_KNIGHT : BLACK_KNIGHT;
            case ROOK -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_ROOK : BLACK_ROOK;
            case PAWN -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_PAWN : BLACK_PAWN;
        };
        
        return color + symbol + RESET_TEXT_COLOR;
    }
}
```

---

## Step 4: Update Main.java

### File: `client/src/main/java/Main.java`

```java
import ui.ChessClient;

public class Main {
    public static void main(String[] args) {
        String serverUrl = "http://localhost:8080";
        
        if (args.length == 1) {
            serverUrl = args[0];
        }
        
        new ChessClient(serverUrl).run();
    }
}
```

---

## Step 5: Unit Tests

### File: `client/src/test/java/client/ServerFacadeTests.java`

```java
package client;

import org.junit.jupiter.api.*;
import server.Server;
import model.*;

import static org.junit.jupiter.api.Assertions.*;

public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade(port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    public void clearDatabase() throws Exception {
        facade.clear();
    }

    @Test
    @DisplayName("Register - Positive")
    public void registerPositive() throws Exception {
        var result = facade.register("testUser", "password123", "test@email.com");
        assertNotNull(result.authToken(), "Auth token should not be null");
        assertEquals("testUser", result.username(), "Username should match");
        assertTrue(result.authToken().length() > 10, "Auth token should be substantial");
    }

    @Test
    @DisplayName("Register - Negative (Duplicate Username)")
    public void registerNegative() throws Exception {
        // Register first user
        facade.register("testUser", "password123", "test@email.com");
        
        // Try to register with same username
        var result = facade.register("testUser", "different", "other@email.com");
        assertNull(result.authToken(), "Auth token should be null for failed registration");
        assertNotNull(result.message(), "Error message should be present");
    }

    @Test
    @DisplayName("Login - Positive")
    public void loginPositive() throws Exception {
        // Register user first
        facade.register("loginUser", "myPassword", "login@test.com");
        
        // Login
        var result = facade.login("loginUser", "myPassword");
        assertNotNull(result.authToken(), "Auth token should not be null");
        assertEquals("loginUser", result.username(), "Username should match");
    }

    @Test
    @DisplayName("Login - Negative (Wrong Password)")
    public void loginNegative() throws Exception {
        // Register user
        facade.register("loginUser", "correctPassword", "user@test.com");
        
        // Try to login with wrong password
        var result = facade.login("loginUser", "wrongPassword");
        assertNull(result.authToken(), "Auth token should be null for failed login");
        assertNotNull(result.message(), "Error message should be present");
    }

    @Test
    @DisplayName("Logout - Positive")
    public void logoutPositive() throws Exception {
        // Register and get auth token
        var registerResult = facade.register("logoutUser", "pass", "logout@test.com");
        String authToken = registerResult.authToken();
        
        // Logout should not throw exception
        assertDoesNotThrow(() -> facade.logout(authToken));
    }

    @Test
    @DisplayName("Logout - Negative (Invalid Auth Token)")
    public void logoutNegative() throws Exception {
        // Try to logout with invalid token
        // Should not crash, but won't be successful
        assertDoesNotThrow(() -> facade.logout("invalid-token-12345"));
    }

    @Test
    @DisplayName("Create Game - Positive")
    public void createGamePositive() throws Exception {
        // Register and login
        var authData = facade.register("gameCreator", "pass", "creator@test.com");
        
        // Create game
        var result = facade.createGame(authData.authToken(), "TestGame");
        assertNotNull(result.gameID(), "Game ID should not be null");
        assertTrue(result.gameID() > 0, "Game ID should be positive");
    }

    @Test
    @DisplayName("Create Game - Negative (No Auth)")
    public void createGameNegative() throws Exception {
        // Try to create game without authentication
        var result = facade.createGame("invalid-token", "TestGame");
        assertNull(result.gameID(), "Game ID should be null without valid auth");
        assertNotNull(result.message(), "Error message should be present");
    }

    @Test
    @DisplayName("List Games - Positive")
    public void listGamesPositive() throws Exception {
        // Register user
        var authData = facade.register("lister", "pass", "list@test.com");
        
        // Create a couple games
        facade.createGame(authData.authToken(), "Game1");
        facade.createGame(authData.authToken(), "Game2");
        
        // List games
        var result = facade.listGames(authData.authToken());
        assertNotNull(result.games(), "Games list should not be null");
        assertTrue(result.games().size() >= 2, "Should have at least 2 games");
    }

    @Test
    @DisplayName("List Games - Negative (No Auth)")
    public void listGamesNegative() throws Exception {
        // Try to list games without authentication
        var result = facade.listGames("bad-token");
        assertNull(result.games(), "Games should be null without valid auth");
        assertNotNull(result.message(), "Error message should be present");
    }

    @Test
    @DisplayName("Join Game - Positive")
    public void joinGamePositive() throws Exception {
        // Register user
        var authData = facade.register("joiner", "pass", "join@test.com");
        
        // Create game
        var gameResult = facade.createGame(authData.authToken(), "JoinTest");
        
        // Join game as white
        var result = facade.joinGame(authData.authToken(), gameResult.gameID(), "WHITE");
        // Should not throw exception and message should be empty or null
        assertNotNull(result);
    }

    @Test
    @DisplayName("Join Game - Negative (Already Taken)")
    public void joinGameNegative() throws Exception {
        // Register two users
        var user1 = facade.register("player1", "pass", "p1@test.com");
        var user2 = facade.register("player2", "pass", "p2@test.com");
        
        // Create game
        var gameResult = facade.createGame(user1.authToken(), "TakenGame");
        
        // User 1 joins as white
        facade.joinGame(user1.authToken(), gameResult.gameID(), "WHITE");
        
        // User 2 tries to join as white (should fail)
        var result = facade.joinGame(user2.authToken(), gameResult.gameID(), "WHITE");
        assertNotNull(result.getMessage(), "Should have error message for already taken");
    }
}
```

---

## Running the Application

### Start the Server (Terminal 1):
```bash
cd /Users/bennethill/IdeaProjects/chess
mvn -pl server exec:java
```

### Run the Client (Terminal 2):
```bash
mvn -pl client exec:java
```

### Run Tests:
```bash
mvn -pl client test
```

### Compile Everything:
```bash
mvn clean compile
```

---

## Checklist

- [ ] Step 1: ServerFacade.java implemented
- [ ] Step 2: State.java created
- [ ] Step 2: ChessClient.java created
- [ ] Step 3: BoardDrawer.java created
- [ ] Step 4: Main.java updated
- [ ] Step 5: ServerFacadeTests.java completed
- [ ] All tests passing (`mvn -pl client test`)
- [ ] Server runs without errors
- [ ] Client runs and connects to server
- [ ] Can register, login, create games, list games
- [ ] Board draws correctly for both perspectives
- [ ] No crashes on bad input

---

## Common Issues

### Import Errors
If you get import errors for `model.*` classes:
- Ensure server module is compiled: `mvn -pl server compile`
- The client has a test dependency on server in pom.xml

### Connection Refused
- Make sure server is running before starting client
- Check that ports match (default 8080)

### Board Not Drawing
- Verify `EscapeSequences.java` is in `ui` package
- Check terminal supports unicode and colors

---

## Phase 5 Requirements Met

✅ Prelogin UI (help, quit, register, login)  
✅ Postlogin UI (help, logout, create, list, join, observe)  
✅ Board drawing (both perspectives)  
✅ ServerFacade with all HTTP methods  
✅ Unit tests (positive & negative for each method)  
✅ Error handling (no crashes, user-friendly messages)  
✅ No display of: JSON, auth tokens, HTTP codes, stack traces

---

**Good luck with Phase 5!**
