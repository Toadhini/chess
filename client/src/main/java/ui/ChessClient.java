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
    private String eval(String line) {
        String[] tokens = line.split("\\s+");
        String command = tokens[0].toLowerCase();
        String[] args = new String[tokens.length - 1];
        System.arraycopy(tokens, 1, args, 0, tokens.length - 1);

        try {
            if (state == State.PRELOGIN) {
                return evalPrelogin(command, args);
            } else {
                return evalPostlogin(command, args);
            }
        } catch (Exception e) {
            return SET_TEXT_COLOR_RED + "Error: " + e.getMessage() + RESET_TEXT_COLOR;
        }
    }

}
