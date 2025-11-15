package model;

public record LoginSessionRequest(String username, String password, String message) {
    public LoginSessionRequest(String username, String password) {
        this(username, password, null);
    }
    public LoginSessionRequest(String message){
        this(null, null, message);
    }
}
