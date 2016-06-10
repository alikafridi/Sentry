package com.hackmit.sentry;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import android.util.Log;

public class MailService{
	private static final String SMTP_HOST_NAME = "smtp.sendgrid.net";
    private static final String SMTP_AUTH_USER = "afridi2@illinois.edu";
    private static final String SMTP_AUTH_PWD  = "";
    private static final String TAG = "MailService";

    public static void main() throws Exception{
    	Log.e(TAG, "Mail Called");
    	new MailService().test();
    }

    public void test() throws Exception{
    	Log.e(TAG, "Test Called");
        Properties props = new Properties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.host", SMTP_HOST_NAME);
        props.put("mail.smtp.port", 587);
        props.put("mail.smtp.auth", "true");
        Log.e(TAG, "Props done");
        Authenticator auth = new SMTPAuthenticator();
        Session mailSession = Session.getDefaultInstance(props, auth);
        // uncomment for debugging infos to stdout
        // mailSession.setDebug(true);
        Transport transport = mailSession.getTransport();

        MimeMessage message = new MimeMessage(mailSession);

        Multipart multipart = new MimeMultipart("alternative");
        Log.e(TAG, "Part 1");
        BodyPart part1 = new MimeBodyPart();
        part1.setText("Sentry has detected a problem at your home.");
        Log.e(TAG, "Part 2");
        BodyPart part2 = new MimeBodyPart();
        part2.setContent("<h1>Sentry has detected a problem at your home.</h1>", "text/html");

        multipart.addBodyPart(part1);
        multipart.addBodyPart(part2);
        Log.e(TAG, "Message Content");
        message.setContent(multipart);
        message.setFrom(new InternetAddress("afridi2@gmail.com"));
        message.setSubject("Sentry Alert:");
        message.addRecipient(Message.RecipientType.TO,
             new InternetAddress("afridi2@illinois.edu")); //7816407891@txt.att.net
        Log.e(TAG, "Transport Connect");
        transport.connect();
        Log.e(TAG, "Sending Message");
        transport.sendMessage(message,
            message.getRecipients(Message.RecipientType.TO));
        Log.e(TAG, "Sent Message");
        transport.close();
    }

    private class SMTPAuthenticator extends javax.mail.Authenticator {
        public PasswordAuthentication getPasswordAuthentication() {
           String username = SMTP_AUTH_USER;
           String password = SMTP_AUTH_PWD;
           return new PasswordAuthentication(username, password);
        }
    }
}
