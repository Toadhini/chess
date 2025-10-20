package model;
//Message used for testing endpoints whether failure or success
public class ResponseMessage {
  private String message;

  public ResponseMessage(String message){
      this.message = message;
  }

  public String getMessage(){
      return message;
  }
}
