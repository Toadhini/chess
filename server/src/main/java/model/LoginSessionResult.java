package model;

public record LoginSessionResult(String username, String authToken, String message) {
    public LoginSessionResult(String username, String authToken){
        this(username, authToken, null);
    }
    public LoginSessionResult(String message){
        this(null, null, message);
    }

}
