/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package secretsantarandomizer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.util.Pair;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 *
 * @author CCannon
 */
public class SecretSantaRandomizer {

    private static ArrayList<Pair> inputList;
    private static ArrayList<Pair> assignmentList;

    private static String emailUserName;
    private static String emailPassword;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        inputList = new ArrayList<>();
        readList(args[0]);
        assignmentList = new ArrayList<>();
        for (Pair pair : inputList) {
            assignmentList.add(pair);
        }
        randomizeAssignmentList();
        readEmailConfig(args[1]);

        String subject = args[2];
        for (int i = 0; i < inputList.size(); i++) {
            String body = "Hey " + inputList.get(i).getKey() + "!"
                    + System.lineSeparator() + System.lineSeparator()
                    + "Your Secret Santa assignment is " + assignmentList.get(i).getKey() + "."
                    + System.lineSeparator() + System.lineSeparator()
                    + "Shhhhh! Don't tell!";
            sendFromGMail((String) inputList.get(i).getValue(), subject, body);
        }
    }

    public static void readList(String filename) {
        try {
            Scanner reader = new Scanner(new File(filename));
            while (reader.hasNext()) {
                String[] inputLine = reader.nextLine().split(",");
                Pair<String, String> entry = new Pair<>(inputLine[0].trim(), inputLine[1].trim());
                inputList.add(entry);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SecretSantaRandomizer.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("No list input file found!");
        }
    }

    public static void readEmailConfig(String filename) {
        try {
            Scanner reader = new Scanner(new File(filename));
            emailUserName = reader.nextLine().trim();
            emailPassword = reader.nextLine().trim();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SecretSantaRandomizer.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("No email config input file found");
        }
    }

    public static void randomizeAssignmentList() {
        boolean duplicatesPresent = true;
        while (duplicatesPresent) {
            Collections.shuffle(assignmentList);
            boolean duplicateFound = false;
            for (int i = 0; i < inputList.size(); i++) {
                if (inputList.get(i).getKey().equals(assignmentList.get(i).getKey())) {
                    duplicateFound = true;
                }
            }
            duplicatesPresent = duplicateFound;
        }
    }

    private static void sendFromGMail(String to, String subject, String body) {
        //Get properties object    
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "587");
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.starttls.enable", "true");
        //get Session   
        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(emailUserName, emailPassword);
            }
        });
        //compose message    
        try {
            MimeMessage message = new MimeMessage(session);
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject(subject);
            message.setText(body);
            //send message  
            Transport transport = session.getTransport("smtp");
            transport.connect(props.getProperty("mail.smtp.host"), emailUserName, emailPassword);
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
            System.out.println("message sent successfully");
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }

    }
}
