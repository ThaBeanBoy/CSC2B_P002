package Mail.Model;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Scanner;

/**
 * A class that contains a static method for sending an email to an SMTP server.
 * @author TG Chipoyera
 * @version P04
 */
public class MailSender {

    /**
     * Writes a message to a PrintWriter and automatically flushes the message
     * @param out The PrintWriter to write to
     * @param message The message
     * @see PrintWriter
     */
    private static void write(PrintWriter out, String message){
        out.println(message);
        out.flush();
    }

    /**
     * Write a file to a stream
     * @param out the OutputStream
     * @param file the file that is meant to be written
     * @throws IOException if an I/O error occurs.
     */
    private static void write(OutputStream out, File file) throws IOException {
        try(FileInputStream fileIn = new FileInputStream(file.getAbsolutePath());){
            byte[] buffer = new byte[1024];
            int bytesRead;
            while((bytesRead = fileIn.read(buffer)) != -1){
                out.write(buffer, 0, bytesRead);
            }

            out.flush();
        }
    }

    /**
     * Write files to a stream
     * @param out the OutputStream
     * @param files the files that are meant to be written
     * @throws IOException if an I/O error occurs.
     */
    private static void write(OutputStream out, File[] files) throws IOException{
        for(File file : files){
            write(out, file);
        }
    }

    /**
     * Gets the response code from an SMTP server
     * @param responseMessage the response message from the Server
     * @return response code
     */
    private static int getSMTPResponseCode(String responseMessage) throws NumberFormatException{
        String codeString = responseMessage.split(" ")[0];
        return Integer.parseInt(codeString);
    }

    private interface  SMTPCommandAction {
        void execute(String response);
    }

    public static void SMTPCommand(PrintWriter out, InputStreamReader in, String message, SMTPCommandAction lambda){
        write(out, message);
        Scanner response = new Scanner(in);
        lambda.execute(response.nextLine());
    }

    /**
     * Method for sending an email to a server
     * @param host the hostname of the server (can also be an IP Address)
     * @param port the port number
     * @param senderEmail the email address of the sender of the email
     * @param receiverEmail the email address of the recipient of the email
     * @param subject the subject line of the email
     * @param message the message of the email
     * @param CCs the cc emails
     * @param attachedFiles the files to be attached
     * @throws FailedConnectionException if an error occurred trying to connect to the hostname & server
     * @throws InvalidEmailException if SMTP server throws an error to an email address
     * @throws MailNotSentException if the email wasn't sent to the SMTP server.
     * @throws IOException if an I/O error occurs.
     */
    public static void SendEmail(
            String host,
            int port,
            String senderEmail,
            String receiverEmail,
            String subject,
            String message,
            String[] CCs,
            File[] attachedFiles
    )
            throws
            FailedConnectionException,
            InvalidEmailException,
            MailNotSentException,
            IOException {
        try(
                // Papercut SMTP running on port 25
                Socket SMTPSocket = new Socket(host, port);

                // Stream communication tools
                PrintWriter out = new PrintWriter(SMTPSocket.getOutputStream(), true);
                InputStreamReader inputStream = new InputStreamReader(
                        new BufferedInputStream(SMTPSocket.getInputStream()),
                        StandardCharsets.UTF_8
                );
        ){
            Scanner in = new Scanner(inputStream);
            String response;
            // Confirming connection to the SMTP server
            response = in.nextLine();
            if(getSMTPResponseCode(response) != 220) throw new FailedConnectionException();

            // Sending connection request
            write(out, String.format("HELO %s", host));
            response = in.nextLine();
            if(getSMTPResponseCode(response) != 250) throw new FailedConnectionException();

            // Setting sender email
            write(out, String.format("MAIL FROM:<%s>", senderEmail.trim()));
            response = in.nextLine();
            if(getSMTPResponseCode(response) != 250) throw new InvalidEmailException(senderEmail);

            // Setting recipient
            write(out, String.format("RCPT TO:<%s>", receiverEmail.trim()));
            response = in.nextLine();
            if(getSMTPResponseCode(response) != 250) throw new InvalidEmailException(receiverEmail);

            // Setting the CCs & making the CC string
            StringBuilder CCEmailsString = new StringBuilder();
            for (int i=0; i<CCs.length; i++){
                String CCEmail = CCs[i];
                // Setting the CC
                write(out, String.format("RCPT TO:<%s>", CCEmail.trim()));
                response = in.nextLine();
                if(getSMTPResponseCode(response) != 250) throw new InvalidEmailException(CCEmail);

                // Adding CC email to the CCEmailsString
                CCEmailsString.append(String.format("%s%s", CCEmail, i==(CCs.length-1) ? ", " : ""));
            }

            write(out, "DATA");
            response = in.nextLine();
            System.out.println(response);

            LocalDate today = LocalDate.now();
            LocalTime now = LocalTime.now();
            String DateTime = String.format("%s %s", today, now);

            write(out, String.format("""
                    From: %s
                    To: %s
                    Cc: %s
                    Date: %s
                    Subject: %s
                    
                    %s
                    \r\n.\r\n""", senderEmail, receiverEmail, CCEmailsString, DateTime, subject, message));

            response = in.nextLine();
            if(getSMTPResponseCode(response) != 250) throw new MailNotSentException();

            // Sending Attached Files
            for(File file : attachedFiles){
                write(out, "DATA");
                response = in.nextLine();
                System.out.println(response);

                write(SMTPSocket.getOutputStream(), file);
                response = in.nextLine();
                System.out.println(response);
            }

            // Closing connection
            write(out, "QUIT");
            response = in.nextLine();
            System.out.println(response);
//            if(getSMTPResponseCode(response) != 221) throw new ConnectionNotCloseException();
        }catch(UnknownHostException exc){
            throw new FailedConnectionException();
        }
    }

}
