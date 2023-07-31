package Mail.Model;

public class MailNotSentException extends Exception{
    public MailNotSentException(){
        super("Could not send email");
    }
}
