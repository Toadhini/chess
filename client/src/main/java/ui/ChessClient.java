package ui;

import client.ServerFacade;
import java.util.Scanner;
import static ui.EscapeSequences.*;

public class ChessClient {
    private final ServerFacade serverFacade;
    private final Scanner scanner;
    private String authToken = null;
    private String username = null;
    private State state = State.PRELOGIN;
    private PostloginUI postloginUI = null;

    private enum State{
        PRELOGIN,
        POSTLOGIN
    }

    public ChessClient(int port){
        this.serverFacade = new ServerFacade(port);
        this.scanner = new Scanner(System.in);
    }

    //Main loop for REPL

    public void run(){
        System.out.println(SET_TEXT_BOLD + "Welcome to my Chess Client!" + RESET_TEXT_BOLD_FAINT);
        System.out.println("Type 'help' to get started");

       boolean running = true;
       while(running){
           try {
               String prompt = getPrompt(); //Create function
               System.out.print(prompt);

               String line = scanner.nextLine().trim();

               if(line.isEmpty()){
                   continue;
               }

               String result = eval(line); //Create function

               if(result.equals("QUIT")){
                   running = false;
               } else if (!result.isEmpty()) {
                   System.out.println(result);
               }

           } catch (Exception e){
               System.out.println(SET_TEXT_COLOR_RED + "ERROR: " + e.getMessage() + RESET_TEXT_COLOR);
           }
       }
       System.out.println("GoodBye! Hope you play again soon!");
       scanner.close();
    }

    //Functions to help loop
    //getPrompt function
    private String getPrompt(){
        if (state == State.PRELOGIN){
            return "\n" + SET_TEXT_COLOR_GREEN + "[LOGGED OUT] >>> " + RESET_TEXT_COLOR;
        } else{
            return "\n" + SET_TEXT_COLOR_BLUE + "[" + username + "] >>> " + RESET_TEXT_COLOR;
        }
    }

    //eval function
    private String eval(String line) throws Exception {
        String[] tokens = line.split("\\s+");
        String command = tokens[0].toLowerCase();
        String[] args = new String[tokens.length - 1];
        System.arraycopy(tokens, 1, args, 0, tokens.length - 1);

        if (state == State.PRELOGIN) {
            return evalPrelogin(command, args);
        } else {
            return evalPostlogin(command, args);
        }
    }

    //To handle pre-login commands
    private String evalPrelogin(String command, String[] args) throws Exception {
        PreloginUI preloginUI = new PreloginUI(serverFacade); //Set up UI after this file

        switch (command) {
            case "help":
                return preloginUI.help();
            case "quit":
                return "QUIT";
            case "register":
                if (args.length != 3) {
                    return "Usage: register <username> <password> <email>";
                }
                var registerResult = preloginUI.register(args[0], args[1], args[2]);
                this.authToken = registerResult.authToken();
                this.username = registerResult.username();
                this.state = State.POSTLOGIN;
                return SET_TEXT_COLOR_GREEN + "Successfully registered and logged in as " + username + RESET_TEXT_COLOR;
            case "login":
                if (args.length != 2) {
                    return "Usage: login <username> <password>";
                }
                var loginResult = preloginUI.login(args[0], args[1]);
                this.authToken = loginResult.authToken();
                this.username = loginResult.username();
                this.state = State.POSTLOGIN;
                return SET_TEXT_COLOR_GREEN + "Successfully logged in as " + username + RESET_TEXT_COLOR;
            default:
                return "Unknown command. Type 'help' for available commands.";
        }
    }
    //To handle post-login commands
    private String evalPostlogin(String command, String[] args) throws Exception {
        if (postloginUI == null) {
            postloginUI = new PostloginUI(serverFacade, authToken);
        }

        switch (command) {
            case "help":
                return postloginUI.help();
            case "logout":
                postloginUI.logout();
                this.authToken = null;
                this.username = null;
                this.state = State.PRELOGIN;
                this.postloginUI = null;
                return SET_TEXT_COLOR_GREEN + "Successfully logged out" + RESET_TEXT_COLOR;
            case "create":
                if (args.length != 1) {
                    return "Usage: create <gameName>";
                }
                return postloginUI.createGame(args[0]);
            case "list":
                return postloginUI.listGames();
            case "play":
                if (args.length != 2) {
                    return "Usage: play <gameNumber> <WHITE|BLACK>";
                }
                try {
                    int gameNum = Integer.parseInt(args[0]);
                    return postloginUI.playGame(gameNum, args[1].toUpperCase());
                } catch (NumberFormatException e) {
                    return "Game number must be a valid integer";
                }
            case "observe":
                if (args.length != 1) {
                    return "Usage: observe <gameNumber>";
                }
                try {
                    int gameNum = Integer.parseInt(args[0]);
                    return postloginUI.observeGame(gameNum);
                } catch (NumberFormatException e) {
                    return "Game number must be a valid integer";
                }
            default:
                return "Unknown command. Type 'help' for available commands.";
        }
    }
}
