package Mail.Model;

public class InvalidEmailException extends Exception{
    private final String invalidEmail;

    public InvalidEmailException(String email){
        super("Invalid Email");
        this.invalidEmail = email;
    }

    public String getInvalidEmail() {
        return invalidEmail;
    }
}
