package Mail.Model;

/**
 * An exception thrown when a socket connection is not closed
 * @author TG Chipoyera
 * @version P04
 */
public class ConnectionNotCloseException extends Exception{
    public ConnectionNotCloseException(){
        super("Connection to server is still open");
    }
}
