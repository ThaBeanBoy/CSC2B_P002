package Mail.Model;

/**
 * An exception that is thrown when there's a failure to connect a socket
 * @author TG Chipoyera
 * @version P04
 */
public class FailedConnectionException extends Exception{
    public FailedConnectionException(){
        super("Could not establish connection");
    }
}
