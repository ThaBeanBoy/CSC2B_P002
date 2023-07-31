package Mail.Model;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Scanner;

/**
 * A class that contains a static method for sending an email to an SMTP server.
 * @author TG Chipoyera
 * @version P04
 */
public class MailSender {
    // Static variables

    static Socket SMTPSocket = null;
    static PrintWriter out = null;
    static InputStreamReader inputStream = null;

    // Static methods

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
     * Gets the response code from an SMTP server
     * @param responseMessage the response message from the Server
     * @return response code
     */
    private static int getSMTPResponseCode(String responseMessage) throws NumberFormatException{
        String codeString = responseMessage.split(" ")[0];
        return Integer.parseInt(codeString);
    }

    /**
     * Made to bve used to run lambda function in the SMTPCommand methods
     */
    private interface  SMTPCommandAction {
        void execute(String response) throws FailedConnectionException, InvalidEmailException, MailNotSentException;
    }

    /**
     * Send a text message to the SMTP server, and get the response in the lambda function.
     * @param message message to the SMTP server
     * @param lambda the lambda function to process the response from the SMTP server
     */
    public static void SMTPCommand(String message, SMTPCommandAction lambda)
    throws FailedConnectionException, InvalidEmailException, MailNotSentException {
        write(out, message);
        Scanner response = new Scanner(inputStream);
        lambda.execute(response.nextLine());
    }

    /**
     * Send a text message to the SMTP server.
     * @param message message to the SMTP server
     */
    public static void SMTPCommand (String message)
    throws MailNotSentException, FailedConnectionException, InvalidEmailException {
        SMTPCommand(message, res -> {});
    }

    /**
     * Send multiple text message to the SMTP server, and get the response to the last message in the lambda
     * @param messages messages to the SMTP server
     * @param lambda the lambda function to process the last response from the SMTP
     */
    public static void SMTPCommands(String[] messages, SMTPCommandAction lambda)
    throws FailedConnectionException, InvalidEmailException, MailNotSentException{
        for(int i=0; i<messages.length; i++){
            String message = messages[i];
            write(out, message);
            if(i==(messages.length-1)){
                Scanner response = new Scanner(inputStream);
                lambda.execute(response.nextLine());
            }
        }
    }

    /**
     * Send multiple text messages to the SMTP server.
     * @param messages messages to the SMTP server
     */
    public static void SMTPCommands(String[] messages)
    throws FailedConnectionException, InvalidEmailException, MailNotSentException{
        SMTPCommands(messages, res -> {});
    }

    /**
     * Returns a response from the SMTP Server
     * @return response
     */
    public static String SMTPServerResponse(){
        Scanner in = new Scanner(inputStream);
        return in.nextLine();
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
        try{
            // Papercut SMTP running on port 25
            SMTPSocket = new Socket(host, port);

            // Stream communication tools
            out = new PrintWriter(SMTPSocket.getOutputStream(), true);
            inputStream = new InputStreamReader(
                    new BufferedInputStream(SMTPSocket.getInputStream()),
                    StandardCharsets.UTF_8
            );

            // Confirming connection to server
            if(getSMTPResponseCode(SMTPServerResponse()) != 220) throw new FailedConnectionException();

            // Sending connection request
            SMTPCommand(String.format("HELO %s", host), (res) -> {
                if (getSMTPResponseCode(res) != 250) throw new FailedConnectionException();
            });

            // Setting sender email
            SMTPCommand(
                String.format("MAIL FROM:<%s>", senderEmail.trim()),
                (res) -> {
                    if(getSMTPResponseCode(res) != 250) throw new InvalidEmailException(senderEmail);
                }
            );

            // Setting recipient
            SMTPCommand(
                String.format("RCPT TO:<%s>", receiverEmail.trim()),
                res -> {
                    if(getSMTPResponseCode(res) != 250) throw new InvalidEmailException(receiverEmail);
                }
            );

            // Setting the CCs & making the CC string
            StringBuilder CCEmailsString = new StringBuilder();
            for (int i=0; i<CCs.length; i++){
                String CCEmail = CCs[i];

                SMTPCommand(
                    String.format("RCPT TO:<%s>", CCEmail.trim()),
                    res -> {
                        if(getSMTPResponseCode(res) != 250) throw new InvalidEmailException(CCEmail);
                    }
                );

                // Adding CC email to the CCEmailsString
                CCEmailsString.append(String.format("%s%s", CCEmail, i==(CCs.length-1) ? ", " : ""));
            }

            // Sending the message
            SMTPCommand("DATA");

            LocalDate today = LocalDate.now();
            LocalTime now = LocalTime.now();
            String DateTime = String.format("%s %s", today, now);

            SMTPCommands(
                new String[]{
                    String.format("From: %s", senderEmail),
                    String.format("To: %s", receiverEmail),
                    String.format("Cc: %s", CCEmailsString),
                    String.format("Date: %s", DateTime),
                    String.format("Subject: %s", subject),
                    "",
                    message,
                    "."
                },
                (res) -> {
                    System.out.println(res);
                    if(getSMTPResponseCode(res) != 250) throw new MailNotSentException();
                }
            );

             // Sending Attached Files
//            for(File file : attachedFiles){
//                SMTPCommand("DATA");
//
//                write(SMTPSocket.getOutputStream(), file);
//                response = in.nextLine();
//                System.out.println(response);
//            }

            // Closing connection
            SMTPCommand("QUIT");
        }catch(UnknownHostException exc){
            throw new FailedConnectionException();
        }finally {
            try {
                // Close the resources in the reverse order of opening
                if (inputStream != null) {
                    inputStream.close();
                }
                if (out != null) {
                    out.close();
                }
                if (SMTPSocket != null) {
                    SMTPSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
